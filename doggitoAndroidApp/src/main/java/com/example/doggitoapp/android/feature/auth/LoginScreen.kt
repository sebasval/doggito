package com.example.doggitoapp.android.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggitoapp.android.core.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Navegar al Home cuando el usuario hace login/registro manualmente
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    DoggitoGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = "Doggito Logo",
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "doggito.",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cuida a tu mejor amigo",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electronico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contrasena") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(email.trim(), password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Iniciar Sesion", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "Olvidaste tu contrasena?",
                            color = DoggitoGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "No tienes cuenta? Registrate",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            TextButton(onClick = onLoginSuccess) {
                Text(
                    "Continuar sin cuenta (modo offline)",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
