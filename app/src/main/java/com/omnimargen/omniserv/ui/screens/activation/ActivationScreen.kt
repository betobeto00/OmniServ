package com.omnimargen.omniserv.ui.screens.activation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.domain.model.LicenseStatus
import com.omnimargen.omniserv.ui.components.CrixtoPaymentButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(
    onActivationSuccess: () -> Unit = {},
    viewModel: ActivationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (uiState.activationSuccess) {
        onActivationSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OmniServ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Sistema de Gestión de Servicios",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show license status or trial info
            when (val status = uiState.licenseStatus) {
                is LicenseStatus.Valid -> {
                    Text(
                        text = "Licencia Activa",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is LicenseStatus.TrialPeriod -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Periodo de Prueba",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Te quedan ${status.daysRemaining} días",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { status.daysRemaining / 7f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Regístrate y paga para mantener acceso completo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is LicenseStatus.Expired -> {
                    Text(
                        text = "Licencia expirada",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is LicenseStatus.InvalidSignature -> {
                    Text(
                        text = "Firma inválida",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StepIndicator(number = 1, label = "Datos", isActive = uiState.currentStep == 1, isCompleted = uiState.currentStep > 1)
                StepIndicator(number = 2, label = "Pago", isActive = uiState.currentStep == 2, isCompleted = uiState.currentStep > 2)
                StepIndicator(number = 3, label = "Listo", isActive = uiState.currentStep == 3, isCompleted = false)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step content
            when (uiState.currentStep) {
                1 -> RegistrationStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    onConfirmRegister = {
                        showConfirmDialog = true
                    }
                )
                2 -> PaymentStep(uiState, viewModel, context)
                3 -> SuccessStep()
            }

            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.register()
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Confirmar Registro") },
            text = {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "¿Los datos son correctos?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Nombre: ${uiState.nombre}")
                    Text(text = "País: ${uiState.pais}")
                    Text(text = "Documento: ${uiState.documento}")
                    Text(text = "Email: ${uiState.email}")
                }
            }
        )
    }
}

@Composable
fun StepIndicator(number: Int, label: String, isActive: Boolean, isCompleted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isCompleted) "✓" else number.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RegistrationStep(
    uiState: ActivationUiState,
    viewModel: ActivationViewModel,
    onConfirmRegister: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tus Datos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = { viewModel.updateNombre(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre completo") },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.pais,
            onValueChange = { viewModel.updatePais(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("País") },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.documento,
            onValueChange = { viewModel.updateDocumento(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Documento (Cédula/RIF)") },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onConfirmRegister() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.nombre.isNotBlank() && uiState.documento.isNotBlank() && uiState.email.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Continuar al Pago")
            }
        }
    }
}

@Composable
fun PaymentStep(uiState: ActivationUiState, viewModel: ActivationViewModel, context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Realizar Pago",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "3 USDT/mes vía CRIXTO",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Al pagar, tu licencia se activará automáticamente",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Crixto payment button
        CrixtoPaymentButton(
            onClick = { viewModel.openCrixto(context) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.paymentPending) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Esperando confirmación de pago...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Skip payment option
        Text(
            text = "¿Quieres probar primero?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { viewModel.skipTrial() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disfrutar 7 días gratis")
        }
    }
}

@Composable
fun SuccessStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Listo!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tu licencia está activa",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
