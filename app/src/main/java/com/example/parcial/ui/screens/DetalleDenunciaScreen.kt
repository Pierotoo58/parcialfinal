package com.example.parcial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parcial.data.repository.NetworkResult
import com.example.parcial.ui.components.ErrorMessage
import com.example.parcial.ui.components.LoadingIndicator
import com.example.parcial.viewmodel.DenunciaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDenunciaScreen(
    denunciaId: Int?,
    viewModel: DenunciaViewModel,
    onNavigateBack: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val marcarAtendidaState by viewModel.marcarAtendidaState.collectAsState()

    var inputIdText by remember { mutableStateOf(denunciaId?.takeIf { it > 0 }?.toString() ?: "") }
    var currentSearchedId by remember { mutableStateOf(denunciaId ?: 0) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(denunciaId) {
        if (denunciaId != null && denunciaId > 0) {
            viewModel.cargarDetalle(denunciaId)
        } else {
            viewModel.resetDetailState()
        }
    }

    LaunchedEffect(marcarAtendidaState) {
        if (marcarAtendidaState is NetworkResult.Success) {
            snackbarHostState.showSnackbar("¡La denuncia fue marcada como ATENDIDA exitosamente!")
            viewModel.resetMarcarAtendidaState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Denuncia") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputIdText,
                    onValueChange = { inputIdText = it.filter { char -> char.isDigit() } },
                    label = { Text("ID de Denuncia") },
                    placeholder = { Text("Ej: 1, 2, 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val parsedId = inputIdText.toIntOrNull()
                        if (parsedId != null && parsedId > 0) {
                            currentSearchedId = parsedId
                            viewModel.cargarDetalle(parsedId)
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buscar ID")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (val state = detailState) {
                null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingresa un ID de denuncia para consultar sus datos.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is NetworkResult.Loading -> {
                    LoadingIndicator(message = "Consultando denuncia #$currentSearchedId...")
                }

                is NetworkResult.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = {
                            if (currentSearchedId > 0) {
                                viewModel.cargarDetalle(currentSearchedId)
                            }
                        }
                    )
                }

                is NetworkResult.Success -> {
                    val denuncia = state.data
                    val isPending = denuncia.estado == 1
                    val statusColor = if (isPending) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    val statusBg = if (isPending) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    val statusText = if (isPending) "PENDIENTE DE ATENCIÓN" else "ATENDIDA"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Denuncia #${denuncia.id}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            DetailItem(label = "DNI del Ciudadano", value = denuncia.dni)
                            DetailItem(label = "Tipo de Incidente", value = denuncia.tipo)
                            DetailItem(label = "Fecha y Hora", value = denuncia.fecha.ifBlank { "No especificada" })
                            DetailItem(label = "Dirección", value = denuncia.direccion)
                            DetailItem(label = "Descripción", value = denuncia.descripcion)
                            DetailItem(
                                label = "Estado numérico",
                                value = "${denuncia.estado} (${if (isPending) "Pendiente" else "Atendida"})"
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (marcarAtendidaState is NetworkResult.Loading) {
                                LoadingIndicator(message = "Actualizando estado a Atendida...")
                            } else {
                                if (marcarAtendidaState is NetworkResult.Error) {
                                    val err = (marcarAtendidaState as NetworkResult.Error).message
                                    ErrorMessage(
                                        message = err,
                                        onRetry = { viewModel.marcarComoAtendida(denuncia.id) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                if (isPending) {
                                    Button(
                                        onClick = { showConfirmDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2E7D32)
                                        )
                                    ) {
                                        Text(
                                            text = "✅ Marcar como Atendida",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Esta denuncia ya ha sido ATENDIDA.",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Confirmar Acción") },
                            text = { Text("¿Deseas marcar la denuncia #${denuncia.id} como ATENDIDA?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showConfirmDialog = false
                                        viewModel.marcarComoAtendida(denuncia.id)
                                    }
                                ) {
                                    Text("Sí, Marcar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
