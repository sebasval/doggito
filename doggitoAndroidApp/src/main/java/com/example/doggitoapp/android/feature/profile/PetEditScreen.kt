package com.example.doggitoapp.android.feature.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.doggitoapp.android.core.theme.*
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetEditScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingPet = uiState.pet

    var name by remember(existingPet) { mutableStateOf(existingPet?.name ?: "") }
    var breed by remember(existingPet) { mutableStateOf(existingPet?.breed ?: "") }
    var weightText by remember(existingPet) { mutableStateOf(existingPet?.weight?.toString() ?: "") }
    var photoUri by remember(existingPet) { mutableStateOf(existingPet?.photoUri) }
    var birthDateMillis by remember(existingPet) { mutableStateOf(existingPet?.birthDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isNew = existingPet == null
    val context = LocalContext.current

    // Photo picker launcher - copies image to internal storage for persistence
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val localPath = copyImageToInternalStorage(context, it)
            if (localPath != null) photoUri = localPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Registrar Mascota" else "Editar Mascota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo selector
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto de mascota",
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
                            contentDescription = null,
                            modifier = Modifier.padding(28.dp),
                            tint = DoggitoGreen
                        )
                    }
                }
                // Camera overlay badge
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = DoggitoGreen,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Toca para agregar foto",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            if (isNew) {
                Text(
                    "Cuentanos sobre tu perro!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(24.dp))
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de tu perro") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Raza") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Birth date field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = birthDateMillis?.let { formatBirthDate(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Fecha de nacimiento") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar fecha",
                                    tint = DoggitoGreen
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = DoggitoGreen
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val weight = weightText.toFloatOrNull() ?: 0f
                            viewModel.savePet(
                                name = name,
                                breed = breed,
                                birthDate = birthDateMillis ?: System.currentTimeMillis(),
                                weight = weight,
                                photoUri = photoUri
                            )
                            onSaved()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = name.isNotBlank() && breed.isNotBlank() && weightText.toFloatOrNull() != null && birthDateMillis != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(if (isNew) "Registrar" else "Guardar Cambios", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    birthDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatBirthDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

/**
 * Copies an image from a content:// URI to the app's internal storage.
 * Returns the absolute file path of the saved image, or null on failure.
 * This ensures the photo persists across app restarts (content:// URIs lose permissions).
 */
private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val photosDir = File(context.filesDir, "pet_photos")
        photosDir.mkdirs()
        val file = File(photosDir, "pet_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
