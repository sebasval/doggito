package com.example.doggitoapp.android.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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

private enum class ResetStep { EMAIL, OTP, NEW_PASSWORD, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Determine the current step based on state
    val currentStep = when {
        uiState.passwordUpdated -> ResetStep.DONE
        uiState.otpVerified -> ResetStep.NEW_PASSWORD
        uiState.resetEmailSent -> ResetStep.OTP
        else -> ResetStep.EMAIL
    }

    // Clear state when leaving
    DisposableEffect(Unit) {
        onDispose { viewModel.clearResetState() }
    }

    DoggitoGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            // Top bar
            if (currentStep != ResetStep.DONE) {
                IconButton(
                    onClick = {
                        when (currentStep) {
                            ResetStep.EMAIL -> onBack()
                            else -> viewModel.clearResetState()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Step indicator
            if (currentStep != ResetStep.DONE) {
                StepIndicator(currentStep)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Content card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        ResetStep.EMAIL -> EmailStep(
                            email = email,
                            onEmailChange = { email = it },
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            onSubmit = {
                                viewModel.clearError()
                                viewModel.resetPassword(email.trim())
                            }
                        )

                        ResetStep.OTP -> OtpStep(
                            email = email,
                            otpCode = otpCode,
                            onOtpChange = { otpCode = it },
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            onSubmit = {
                                viewModel.clearError()
                                viewModel.verifyRecoveryOtp(email.trim(), otpCode.trim())
                            },
                            onResend = {
                                viewModel.clearError()
                                viewModel.resetPassword(email.trim())
                            }
                        )

                        ResetStep.NEW_PASSWORD -> NewPasswordStep(
                            newPassword = newPassword,
                            confirmPassword = confirmPassword,
                            showPassword = showPassword,
                            onNewPasswordChange = { newPassword = it },
                            onConfirmPasswordChange = { confirmPassword = it },
                            onToggleShowPassword = { showPassword = !showPassword },
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            onSubmit = {
                                viewModel.clearError()
                                viewModel.updatePassword(newPassword)
                            }
                        )

                        ResetStep.DONE -> DoneStep(onBackToLogin = onBackToLogin)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StepIndicator(currentStep: ResetStep) {
    val stepNumber = when (currentStep) {
        ResetStep.EMAIL -> 1
        ResetStep.OTP -> 2
        ResetStep.NEW_PASSWORD -> 3
        ResetStep.DONE -> 3
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..3) {
            Box(
                modifier = Modifier
                    .size(if (i == stepNumber) 12.dp else 8.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(50),
                    color = if (i <= stepNumber) Color.White else Color.White.copy(alpha = 0.3f)
                ) {}
            }
            if (i < 3) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit
) {
    Text(
        text = "Recuperar contrasena",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Ingresa tu correo electronico y te enviaremos un codigo de verificacion.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Correo electronico") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )

    ErrorMessage(error)

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = email.isNotBlank() && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Text("Enviar codigo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OtpStep(
    email: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit,
    onResend: () -> Unit
) {
    Text(
        text = "Verificar codigo",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Enviamos un codigo de verificacion a:",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Text(
        text = email,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = otpCode,
        onValueChange = { if (it.all { c -> c.isDigit() }) onOtpChange(it) },
        label = { Text("Codigo de verificacion") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            letterSpacing = 4.sp
        )
    )

    ErrorMessage(error)

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = otpCode.length >= 6 && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Text("Verificar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = onResend) {
        Text(
            "No recibiste el codigo? Reenviar",
            color = DoggitoGreen,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    showPassword: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleShowPassword: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit
) {
    val passwordsMatch = newPassword == confirmPassword
    val isValid = newPassword.length >= 6 && passwordsMatch && confirmPassword.isNotBlank()

    Text(
        text = "Nueva contrasena",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Ingresa tu nueva contrasena. Debe tener al menos 6 caracteres.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        label = { Text("Nueva contrasena") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleShowPassword) {
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

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirmar contrasena") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        isError = confirmPassword.isNotBlank() && !passwordsMatch
    )

    if (confirmPassword.isNotBlank() && !passwordsMatch) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Las contrasenas no coinciden",
            color = ErrorRed,
            style = MaterialTheme.typography.bodySmall
        )
    }

    ErrorMessage(error)

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = isValid && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Text("Actualizar contrasena", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DoneStep(onBackToLogin: () -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))

    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = DoggitoGreen
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Contrasena actualizada",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Tu contrasena ha sido actualizada correctamente. Ya puedes iniciar sesion con tu nueva contrasena.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onBackToLogin,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DoggitoGreen)
    ) {
        Text("Volver a iniciar sesion", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ErrorMessage(error: String?) {
    if (error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            color = ErrorRed,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
