package com.example.parcial.data.remote

object ApiConfig {
    // URL base para el emulador conectándose a la API PHP en XAMPP / MySQL
    // http://10.0.2.2/denuncias_api/index.php/
    private var _baseUrl: String = "http://10.0.2.2/denuncias_api/index.php/"

    var baseUrl: String
        get() = _baseUrl
        set(value) {
            _baseUrl = if (value.endsWith("/")) value else "$value/"
            RetrofitClient.resetClient()
        }
}

