package com.example.parcial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.parcial.data.repository.NetworkResult
import com.example.parcial.ui.components.DenunciaCard
import com.example.parcial.ui.components.ErrorMessage
import com.example.parcial.ui.components.LoadingIndicator
import com.example.parcial.viewmodel.DenunciaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarPorDniScreen(
    viewModel: DenunciaViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val dniSearchState by viewModel.dniSearchState.collectAsState()

    var searchDni by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar por DNI") },
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
        ) {
            Text(
                text = "Consultar Denuncias de un Ciudadano",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ingresa el DNI de 8 dígitos para consultar todas sus denuncias asociadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchDni,
                    onValueChange = {
                        if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                            searchDni = it
                            inputError = null
                        }
                    },
                    label = { Text("DNI (8 dígitos)") },
                    isError = inputError != null,
                    supportingText = {
                        if (inputError != null) {
                            Text(inputError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (searchDni.length == 8) {
                            inputError = null
                            viewModel.buscarPorDni(searchDni)
                        } else {
                            inputError = "Debe ingresar 8 dígitos"
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buscar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = dniSearchState) {
                null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingresa un DNI y presiona 'Buscar'.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is NetworkResult.Loading -> {
                    LoadingIndicator(message = "Buscando denuncias para DNI: $searchDni...")
                }

                is NetworkResult.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.buscarPorDni(searchDni) }
                    )
                }

                is NetworkResult.Success -> {
                    val denuncias = state.data
                    if (denuncias.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron denuncias registradas para el DNI: $searchDni",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Se encontraron ${denuncias.size} denuncia(s) para el DNI $searchDni:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = denuncias,
                                key = { it.id }
                            ) { denuncia ->
                                DenunciaCard(
                                    denuncia = denuncia,
                                    onClick = { onNavigateToDetail(denuncia.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
