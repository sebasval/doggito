// Supabase Edge Function: send-notification
// Envia notificaciones push via Firebase Cloud Messaging (FCM) HTTP v1 API
//
// Uso desde Supabase Dashboard o curl:
// POST /functions/v1/send-notification
// Headers: Authorization: Bearer <SUPABASE_ANON_KEY>
// Body: {
//   "user_ids": ["uuid1", "uuid2"] o "all" para todos,
//   "title": "Titulo de la notificacion",
//   "body": "Cuerpo del mensaje",
//   "deep_link": "shop/producto123" (opcional),
//   "channel": "rewards" | "reminders" (opcional, default: "rewards")
// }

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const FIREBASE_SERVICE_ACCOUNT = JSON.parse(
  Deno.env.get("FIREBASE_SERVICE_ACCOUNT")!
);

// --- Utilidades para generar OAuth2 token desde Service Account ---

function base64url(source: ArrayBuffer): string {
  const bytes = new Uint8Array(source);
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

async function createJwt(serviceAccount: any): Promise<string> {
  const now = Math.floor(Date.now() / 1000);

  const header = {
    alg: "RS256",
    typ: "JWT",
  };

  const payload = {
    iss: serviceAccount.client_email,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    iat: now,
    exp: now + 3600,
  };

  const encodedHeader = base64url(
    new TextEncoder().encode(JSON.stringify(header))
  );
  const encodedPayload = base64url(
    new TextEncoder().encode(JSON.stringify(payload))
  );

  const unsignedToken = `${encodedHeader}.${encodedPayload}`;

  // Importar la clave privada RSA
  const pemContents = serviceAccount.private_key
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\n/g, "");

  const binaryKey = Uint8Array.from(atob(pemContents), (c) => c.charCodeAt(0));

  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryKey,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    new TextEncoder().encode(unsignedToken)
  );

  return `${unsignedToken}.${base64url(signature)}`;
}

async function getAccessToken(serviceAccount: any): Promise<string> {
  const jwt = await createJwt(serviceAccount);

  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });

  const data = await response.json();
  if (!data.access_token) {
    throw new Error(`Error obteniendo access token: ${JSON.stringify(data)}`);
  }

  return data.access_token;
}

// --- Funcion para enviar mensaje FCM ---

async function sendFcmMessage(
  accessToken: string,
  projectId: string,
  fcmToken: string,
  title: string,
  body: string,
  deepLink?: string,
  channel?: string
): Promise<boolean> {
  const message: any = {
    message: {
      token: fcmToken,
      data: {
        title,
        body,
        ...(deepLink && { deep_link: deepLink }),
        ...(channel && { channel }),
      },
      android: {
        priority: "high",
      },
    },
  };

  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(message),
    }
  );

  if (!response.ok) {
    const error = await response.text();
    console.error(`Error enviando a token ${fcmToken.substring(0, 20)}...: ${error}`);
    return false;
  }

  return true;
}

// --- Handler principal ---

serve(async (req) => {
  try {
    // Verificar metodo
    if (req.method !== "POST") {
      return new Response(JSON.stringify({ error: "Metodo no permitido" }), {
        status: 405,
        headers: { "Content-Type": "application/json" },
      });
    }

    const { user_ids, title, body, deep_link, channel } = await req.json();

    if (!title || !body) {
      return new Response(
        JSON.stringify({ error: "title y body son requeridos" }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    // Crear cliente Supabase con service role para leer todos los tokens
    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    // Obtener tokens de dispositivos
    let query = supabase.from("device_tokens").select("fcm_token, user_id");

    if (user_ids && user_ids !== "all" && Array.isArray(user_ids)) {
      query = query.in_("user_id", user_ids);
    }

    const { data: tokens, error: dbError } = await query;

    if (dbError) {
      return new Response(
        JSON.stringify({ error: `Error consultando tokens: ${dbError.message}` }),
        { status: 500, headers: { "Content-Type": "application/json" } }
      );
    }

    if (!tokens || tokens.length === 0) {
      return new Response(
        JSON.stringify({ message: "No hay dispositivos registrados", sent: 0 }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    }

    // Obtener access token de FCM
    const accessToken = await getAccessToken(FIREBASE_SERVICE_ACCOUNT);
    const projectId = FIREBASE_SERVICE_ACCOUNT.project_id;

    // Enviar a todos los tokens
    let sent = 0;
    let failed = 0;
    const errors: string[] = [];

    for (const tokenRecord of tokens) {
      const success = await sendFcmMessage(
        accessToken,
        projectId,
        tokenRecord.fcm_token,
        title,
        body,
        deep_link,
        channel
      );

      if (success) {
        sent++;
      } else {
        failed++;
        // Opcional: eliminar tokens invalidos
        errors.push(tokenRecord.fcm_token.substring(0, 20) + "...");
      }
    }

    return new Response(
      JSON.stringify({
        message: `Notificaciones enviadas`,
        total: tokens.length,
        sent,
        failed,
        ...(errors.length > 0 && { failed_tokens: errors }),
      }),
      { status: 200, headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Error en Edge Function:", error);
    return new Response(
      JSON.stringify({ error: error.message || "Error interno" }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
