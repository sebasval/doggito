package com.example.doggitoapp.android.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.core.util.DateUtils
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    onEdit: (String) -> Unit,
    onMedicalHistory: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Mascota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    uiState.pet?.let { pet ->
                        IconButton(onClick = { onEdit(pet.id) }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        val pet = uiState.pet

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoggitoGreen)
            }
        } else if (pet == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Pets, null, Modifier.size(80.dp), tint = DoggitoGreenLight)
                    Spacer(Modifier.height(16.dp))
                    Text("Aun no has registrado a tu mascota", color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onEdit("") },
                        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Agregar Mascota")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pet.photoUri != null) {
                    AsyncImage(
                        model = pet.photoUri,
                        contentDescription = pet.name,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = DoggitoGreenLight.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            null,
                            Modifier.padding(24.dp),
                            tint = DoggitoGreenDark
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(pet.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(pet.breed, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    InfoChipWithSubtitle(
                        icon = Icons.Default.Cake,
                        label = "Edad",
                        value = calculateAge(pet.birthDate),
                        subtitle = DateUtils.formatDate(pet.birthDate)
                    )
                    InfoChip(Icons.Default.Scale, "Peso", "${pet.weight} kg")
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    onClick = { onMedicalHistory(pet.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MedicalServices, null, tint = HealthColor)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Historial Medico", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                "${uiState.vaccines.size} registros de vacunas",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = DoggitoGreen)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun InfoChipWithSubtitle(icon: ImageVector, label: String, value: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = DoggitoGreen)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

private fun calculateAge(birthDateMillis: Long): String {
    val birth = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
    val now = Calendar.getInstance()
    var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
    if (now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
        months--
    }
    if (months < 0) {
        years--
        months += 12
    }
    return when {
        years > 0 -> "$years ${if (years == 1) "año" else "años"} $months ${if (months == 1) "mes" else "meses"}"
        months > 0 -> "$months ${if (months == 1) "mes" else "meses"}"
        else -> "Recien nacido"
    }
}
