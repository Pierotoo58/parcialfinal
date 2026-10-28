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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parcial.data.repository.NetworkResult
import com.example.parcial.ui.components.DenunciaCard
import com.example.parcial.ui.components.ErrorMessage
import com.example.parcial.ui.components.LoadingIndicator
import com.example.parcial.viewmodel.DenunciaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDenunciasScreen(
    viewModel: DenunciaViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val denunciasState by viewModel.denunciasState.collectAsState()
    val denunciasAcumuladas by viewModel.denunciasAcumuladas.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()
    val isLoadingPage by viewModel.isLoadingPage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Denuncias") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.loadInitialPage() }) {
                        Text("🔄 Recargar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (denunciasAcumuladas.isEmpty() && denunciasState is NetworkResult.Loading) {
                LoadingIndicator(message = "Cargando lista de denuncias...")
            } else if (denunciasAcumuladas.isEmpty() && denunciasState is NetworkResult.Error) {
                val error = (denunciasState as NetworkResult.Error).message
                ErrorMessage(
                    message = error,
                    onRetry = { viewModel.loadInitialPage() }
                )
            } else if (denunciasAcumuladas.isEmpty() && denunciasState is NetworkResult.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay denuncias registradas.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = denunciasAcumuladas,
                        key = { it.id }
                    ) { denuncia ->
                        DenunciaCard(
                            denuncia = denuncia,
                            onClick = { onNavigateToDetail(denuncia.id) }
                        )
                    }

                    item {
                        if (denunciasState is NetworkResult.Error) {
                            val err = (denunciasState as NetworkResult.Error).message
                            ErrorMessage(
                                message = err,
                                onRetry = { viewModel.loadNextPage() }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingPage) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                                    Text("Cargando página...")
                                }
                            } else if (hasMorePages) {
                                Button(
                                    onClick = { viewModel.loadNextPage() },
                                    enabled = !isLoadingPage,
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(48.dp)
                                ) {
                                    Text("Cargar siguientes ${viewModel.pageLimit}")
                                }
                            } else {
                                Text(
                                    text = "Fin de los resultados (${denunciasAcumuladas.size} denuncias en total)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
