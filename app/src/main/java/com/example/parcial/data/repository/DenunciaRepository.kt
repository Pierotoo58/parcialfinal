package com.example.parcial.data.repository

import com.example.parcial.data.model.Denuncia
import com.example.parcial.data.model.DenunciaRequest
import com.example.parcial.data.remote.RetrofitClient
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

class DenunciaRepository {

    private val apiService get() = RetrofitClient.apiService

    suspend fun getDenuncias(offset: Int = 0, limit: Int = 5): NetworkResult<List<Denuncia>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDenuncias(offset, limit)
            if (response.isSuccessful) {
                val list = parseDenunciaList(response.body())
                NetworkResult.Success(list)
            } else {
                NetworkResult.Error(
                    message = extractErrorMessage(response),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo conectar al servidor"}")
        }
    }

    suspend fun registrarDenuncia(request: DenunciaRequest): NetworkResult<Denuncia> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registrarDenuncia(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(
                    message = extractErrorMessage(response),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo conectar al servidor"}")
        }
    }

    suspend fun getDenunciaById(id: Int): NetworkResult<Denuncia> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDenunciaById(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else if (response.code() == 404) {
                val errorMsg = extractErrorMessage(response)
                NetworkResult.Error(if (errorMsg.contains("Error")) "No se encontró la denuncia con ID: $id" else errorMsg, code = 404)
            } else {
                NetworkResult.Error(
                    message = extractErrorMessage(response),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo conectar al servidor"}")
        }
    }

    suspend fun getDenunciasByDni(dni: String): NetworkResult<List<Denuncia>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDenunciasByDni(dni)
            if (response.isSuccessful) {
                val list = parseDenunciaList(response.body())
                NetworkResult.Success(list)
            } else if (response.code() == 404) {
                NetworkResult.Success(emptyList())
            } else {
                NetworkResult.Error(
                    message = extractErrorMessage(response),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo conectar al servidor"}")
        }
    }

    suspend fun marcarAtendida(id: Int): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.marcarAtendida(id)
            if (response.isSuccessful) {
                return@withContext NetworkResult.Success(true)
            }

            if (response.code() == 400 || response.code() == 415 || response.code() == 405) {
                val fallbackResponse = apiService.marcarAtendidaConCuerpo(id, mapOf("estado" to 0))
                if (fallbackResponse.isSuccessful) {
                    return@withContext NetworkResult.Success(true)
                }
            }

            NetworkResult.Error(
                message = extractErrorMessage(response),
                code = response.code()
            )
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo actualizar el estado"}")
        }
    }

    private fun extractErrorMessage(response: retrofit2.Response<*>): String {
        try {
            val errorString = response.errorBody()?.string()
            if (!errorString.isNullOrBlank()) {
                val json = RetrofitClient.gson.fromJson(errorString, com.google.gson.JsonObject::class.java)
                if (json != null && json.has("error")) {
                    return json.get("error").asString
                }
            }
        } catch (_: Exception) {}
        return "Error ${response.code()}: ${response.message()}"
    }

    private fun parseDenunciaList(element: JsonElement?): List<Denuncia> {
        if (element == null || element.isJsonNull) return emptyList()

        if (element.isJsonArray) {
            val type = object : TypeToken<List<Denuncia>>() {}.type
            return RetrofitClient.gson.fromJson(element, type) ?: emptyList()
        }

        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val arrayKeys = listOf("content", "data", "denuncias", "items", "results")
            for (key in arrayKeys) {
                if (obj.has(key) && obj.get(key).isJsonArray) {
                    val type = object : TypeToken<List<Denuncia>>() {}.type
                    return RetrofitClient.gson.fromJson(obj.get(key), type) ?: emptyList()
                }
            }
            if (obj.has("dni") || obj.has("id")) {
                val single = RetrofitClient.gson.fromJson(obj, Denuncia::class.java)
                if (single != null) return listOf(single)
            }
        }

        return emptyList()
    }
}
