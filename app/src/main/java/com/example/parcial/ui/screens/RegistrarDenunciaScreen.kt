package com.example.parcial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.parcial.data.repository.NetworkResult
import com.example.parcial.ui.components.ErrorMessage
import com.example.parcial.ui.components.LoadingIndicator
import com.example.parcial.viewmodel.DenunciaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.graphics.Color
import com.example.parcial.ui.theme.HeroGradientStart
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarDenunciaScreen(
    viewModel: DenunciaViewModel,
    onNavigateBack: () -> Unit
) {
    val registrationState by viewModel.registrationState.collectAsState()

    var dni by remember { mutableStateOf("") }
    var fecha by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
    }
    var tipo by remember { mutableStateOf("ROBO") }
    var direccion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var dniError by remember { mutableStateOf<String?>(null) }
    var direccionError by remember { mutableStateOf<String?>(null) }
    var descripcionError by remember { mutableStateOf<String?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var expandedTipoDropdown by remember { mutableStateOf(false) }

    val tiposList = listOf("ROBO", "ACCIDENTE", "RUIDO_MOLESTO", "BASURA", "VIOLENCIA", "OTRO")

    LaunchedEffect(registrationState) {
        if (registrationState is NetworkResult.Success) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("➕ Registrar Denuncia", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeroGradientStart
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Nueva Denuncia Ciudadana",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Completa los datos de la denuncia. El estado inicial se registrará como Pendiente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dni,
                onValueChange = {
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        dni = it
                        dniError = null
                    }
                },
                label = { Text("DNI del Ciudadano (8 dígitos)") },
                isError = dniError != null,
                supportingText = {
                    if (dniError != null) {
                        Text(dniError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${dni.length}/8 dígitos")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha y Hora") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expandedTipoDropdown,
                onExpandedChange = { expandedTipoDropdown = !expandedTipoDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Denuncia") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoDropdown) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTipoDropdown,
                    onDismissRequest = { expandedTipoDropdown = false }
                ) {
                    tiposList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                tipo = item
                                expandedTipoDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = {
                    if (it.length <= 200) {
                        direccion = it
                        direccionError = null
                    }
                },
                label = { Text("Dirección de la Denuncia") },
                isError = direccionError != null,
                supportingText = {
                    if (direccionError != null) {
                        Text(direccionError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${direccion.length}/200 caracteres")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    if (it.length <= 255) {
                        descripcion = it
                        descripcionError = null
                    }
                },
                label = { Text("Descripción de la Denuncia") },
                isError = descripcionError != null,
                supportingText = {
                    if (descripcionError != null) {
                        Text(descripcionError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${descripcion.length}/255 caracteres")
                    }
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (registrationState is NetworkResult.Loading) {
                LoadingIndicator(message = "Enviando denuncia...")
            } else {
                if (registrationState is NetworkResult.Error) {
                    val message = (registrationState as NetworkResult.Error).message
                    ErrorMessage(
                        message = message,
                        onRetry = null
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        var isValid = true
                        if (dni.length != 8) {
                            dniError = "El DNI debe contener exactamente 8 dígitos"
                            isValid = false
                        }
                        if (direccion.isBlank()) {
                            direccionError = "La dirección es requerida"
                            isValid = false
                        }
                        if (descripcion.isBlank()) {
                            descripcionError = "La descripción es requerida"
                            isValid = false
                        }

                        if (isValid) {
                            viewModel.registrarDenuncia(
                                dni = dni,
                                fecha = fecha,
                                tipo = tipo,
                                direccion = direccion,
                                descripcion = descripcion
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrar Denuncia", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showSuccessDialog) {
        val registeredItem = (registrationState as? NetworkResult.Success)?.data
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetRegistrationState()
            },
            title = { Text("¡Denuncia Registrada!") },
            text = {
                Text(
                    "La denuncia se registró con éxito con el ID #${registeredItem?.id ?: "asignado"}.\n" +
                    "El estado inicial es PENDIENTE."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetRegistrationState()
                        dni = ""
                        direccion = ""
                        descripcion = ""
                        fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}
