package com.example.parcial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial.data.model.Denuncia
import com.example.parcial.data.model.DenunciaRequest
import com.example.parcial.data.remote.ApiConfig
import com.example.parcial.data.repository.DenunciaRepository
import com.example.parcial.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DenunciaViewModel(
    private val repository: DenunciaRepository = DenunciaRepository()
) : ViewModel() {

    // Lista Paginada
    private val _denunciasState = MutableStateFlow<NetworkResult<List<Denuncia>>>(NetworkResult.Success(emptyList()))
    val denunciasState: StateFlow<NetworkResult<List<Denuncia>>> = _denunciasState.asStateFlow()

    private val _denunciasAcumuladas = MutableStateFlow<List<Denuncia>>(emptyList())
    val denunciasAcumuladas: StateFlow<List<Denuncia>> = _denunciasAcumuladas.asStateFlow()

    var currentOffset = 0
        private set
    val pageLimit = 5

    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _isLoadingPage = MutableStateFlow(false)
    val isLoadingPage: StateFlow<Boolean> = _isLoadingPage.asStateFlow()

    // Registro
    private val _registrationState = MutableStateFlow<NetworkResult<Denuncia>?>(null)
    val registrationState: StateFlow<NetworkResult<Denuncia>?> = _registrationState.asStateFlow()

    // Detalle
    private val _detailState = MutableStateFlow<NetworkResult<Denuncia>?>(null)
    val detailState: StateFlow<NetworkResult<Denuncia>?> = _detailState.asStateFlow()

    // Búsqueda DNI
    private val _dniSearchState = MutableStateFlow<NetworkResult<List<Denuncia>>?>(null)
    val dniSearchState: StateFlow<NetworkResult<List<Denuncia>>?> = _dniSearchState.asStateFlow()

    // Marcar como atendida
    private val _marcarAtendidaState = MutableStateFlow<NetworkResult<Boolean>?>(null)
    val marcarAtendidaState: StateFlow<NetworkResult<Boolean>?> = _marcarAtendidaState.asStateFlow()

    init {
        loadInitialPage()
    }

    fun loadInitialPage() {
        currentOffset = 0
        _hasMorePages.value = true
        _denunciasAcumuladas.value = emptyList()
        fetchPage(offset = 0)
    }

    fun loadNextPage() {
        if (_isLoadingPage.value || !_hasMorePages.value) return
        fetchPage(offset = currentOffset)
    }

    private fun fetchPage(offset: Int) {
        viewModelScope.launch {
            _isLoadingPage.value = true
            _denunciasState.value = NetworkResult.Loading

            val result = repository.getDenuncias(offset = offset, limit = pageLimit)
            _isLoadingPage.value = false
            _denunciasState.value = result

            if (result is NetworkResult.Success) {
                val newItems = result.data
                if (newItems.isEmpty() || newItems.size < pageLimit) {
                    _hasMorePages.value = false
                }
                _denunciasAcumuladas.value = _denunciasAcumuladas.value + newItems
                currentOffset += pageLimit
            }
        }
    }

    fun registrarDenuncia(dni: String, fecha: String, tipo: String, direccion: String, descripcion: String) {
        viewModelScope.launch {
            _registrationState.value = NetworkResult.Loading
            val request = DenunciaRequest(
                dni = dni,
                fecha = fecha,
                tipo = tipo,
                direccion = direccion,
                descripcion = descripcion
            )
            val result = repository.registrarDenuncia(request)
            _registrationState.value = result
            if (result is NetworkResult.Success) {
                loadInitialPage()
            }
        }
    }

    fun resetRegistrationState() {
        _registrationState.value = null
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _detailState.value = NetworkResult.Loading
            val result = repository.getDenunciaById(id)
            _detailState.value = result
        }
    }

    fun resetDetailState() {
        _detailState.value = null
    }

    fun buscarPorDni(dni: String) {
        viewModelScope.launch {
            _dniSearchState.value = NetworkResult.Loading
            val result = repository.getDenunciasByDni(dni)
            _dniSearchState.value = result
        }
    }

    fun resetDniSearchState() {
        _dniSearchState.value = null
    }

    fun marcarComoAtendida(id: Int) {
        viewModelScope.launch {
            _marcarAtendidaState.value = NetworkResult.Loading
            val result = repository.marcarAtendida(id)
            _marcarAtendidaState.value = result

            if (result is NetworkResult.Success) {
                _detailState.value?.let { currentDetail ->
                    if (currentDetail is NetworkResult.Success && currentDetail.data.id == id) {
                        _detailState.value = NetworkResult.Success(currentDetail.data.copy(estado = 0))
                    }
                }
                _denunciasAcumuladas.value = _denunciasAcumuladas.value.map { item ->
                    if (item.id == id) item.copy(estado = 0) else item
                }
            }
        }
    }

    fun resetMarcarAtendidaState() {
        _marcarAtendidaState.value = null
    }

    fun setBaseUrl(newUrl: String) {
        ApiConfig.baseUrl = newUrl
        loadInitialPage()
    }
}
