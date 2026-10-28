package com.example.parcial.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private var instance: DenunciaApiService? = null

    val apiService: DenunciaApiService
        get() {
            if (instance == null) {
                instance = createApiService()
            }
            return instance!!
        }

    fun resetClient() {
        instance = null
    }

    private fun createApiService(): DenunciaApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(DenunciaApiService::class.java)
    }
}
