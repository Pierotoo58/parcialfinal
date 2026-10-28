package com.example.parcial.data.remote

import com.example.parcial.data.model.Denuncia
import com.example.parcial.data.model.DenunciaRequest
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DenunciaApiService {

    @GET("denuncias")
    suspend fun getDenuncias(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<JsonElement>

    @POST("denuncias")
    suspend fun registrarDenuncia(
        @Body denuncia: DenunciaRequest
    ): Response<Denuncia>

    @GET("denuncias/{id}")
    suspend fun getDenunciaById(
        @Path("id") id: Int
    ): Response<Denuncia>

    @GET("denuncias/ciudadano/{dni}")
    suspend fun getDenunciasByDni(
        @Path("dni") dni: String
    ): Response<JsonElement>

    @Headers("Content-Length: 0")
    @PUT("denuncias/{id}")
    suspend fun marcarAtendida(
        @Path("id") id: Int
    ): Response<JsonElement>

    @PUT("denuncias/{id}")
    suspend fun marcarAtendidaConCuerpo(
        @Path("id") id: Int,
        @Body body: Map<String, Int>
    ): Response<JsonElement>
}
