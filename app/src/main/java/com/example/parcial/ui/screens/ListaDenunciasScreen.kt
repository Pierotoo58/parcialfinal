package com.example.parcial.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parcial.data.repository.NetworkResult
import com.example.parcial.ui.components.DenunciaCard
import com.example.parcial.ui.components.ErrorMessage
import com.example.parcial.ui.components.LoadingIndicator
import com.example.parcial.ui.theme.HeroGradientStart
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
                title = {
                    Text(
                        text = "📋 Lista de Denuncias",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.loadInitialPage() }) {
                        Text("🔄 Recargar", color = Color.White)
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (denunciasAcumuladas.isEmpty() && denunciasState is NetworkResult.Loading) {
                LoadingIndicator(message = "Cargando denuncias desde MySQL...")
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
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📭", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay denuncias en el servidor.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Usa el formulario para registrar la primera denuncia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                                    Text("Cargando siguientes 5...")
                                }
                            } else if (hasMorePages) {
                                Button(
                                    onClick = { viewModel.loadNextPage() },
                                    enabled = !isLoadingPage,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Cargar siguientes ${viewModel.pageLimit}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "✨ Todos los resultados cargados (${denunciasAcumuladas.size} en total)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
