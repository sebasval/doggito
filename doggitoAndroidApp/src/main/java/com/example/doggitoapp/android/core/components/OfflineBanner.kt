package com.example.doggitoapp.android.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.TextPrimary
import com.example.doggitoapp.android.core.theme.WarningAmber

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WarningAmber.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = "Sin conexion",
                tint = WarningAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Modo offline - tus datos se sincronizaran al conectarte",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
        }
    }
}
