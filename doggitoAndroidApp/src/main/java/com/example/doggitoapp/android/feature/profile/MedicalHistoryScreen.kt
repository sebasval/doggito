package com.example.doggitoapp.android.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.DoggitoOrange
import com.example.doggitoapp.android.core.theme.HealthColor
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.domain.model.VaccineRecord
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    petId: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Médico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DoggitoOrange
            ) {
                Icon(Icons.Default.Add, "Agregar vacuna")
            }
        }
    ) { padding ->
        if (uiState.vaccines.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MedicalServices, null, Modifier.size(64.dp), tint = HealthColor)
                    Spacer(Modifier.height(16.dp))
                    Text("Sin registros médicos aún")
                    Text(
                        "Agrega las vacunas de tu mascota",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.vaccines) { vaccine ->
                    VaccineCard(vaccine = vaccine, onDelete = { viewModel.deleteVaccine(vaccine) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddVaccineDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, vetName ->
                viewModel.addVaccine(
                    vaccineName = name,
                    dateAdministered = System.currentTimeMillis(),
                    nextDueDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
                    vetName = vetName.ifBlank { null }
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun VaccineCard(vaccine: VaccineRecord, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Vaccines, null, tint = HealthColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(vaccine.vaccineName, fontWeight = FontWeight.SemiBold)
                Text(
                    "Aplicada: ${DateUtils.formatDate(vaccine.dateAdministered)}",
                    style = MaterialTheme.typography.bodySmall
                )
                vaccine.nextDueDate?.let {
                    Text(
                        "Próxima: ${DateUtils.formatDate(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                vaccine.vetName?.let {
                    Text("Vet: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddVaccineDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var vaccineName by remember { mutableStateOf("") }
    var vetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Vacuna") },
        text = {
            Column {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Nombre de la vacuna") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vetName,
                    onValueChange = { vetName = it },
                    label = { Text("Veterinario (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(vaccineName, vetName) },
                enabled = vaccineName.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
