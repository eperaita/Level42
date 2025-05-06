package com.example.intrapp

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


class ApiClient {

    private val client: HttpClient = HttpClient()

    // Función para enviar solicitudes POST con body
    suspend fun post(url: String, body: String): HttpResponse? {
        return try {
            client.post(url) {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(body)
            }
        } catch (e: Exception) {
            println("[ApiClient] Error en la solicitud POST: ${e.message}")
            null
        }
    }

    // Función para enviar solicitudes GET con headers
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse? {
        return try {
            client.get(url) {
                headers.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }
        } catch (e: Exception) {
            println("[ApiClient] Error en la solicitud GET: ${e.message}")
            null
        }
    }

    suspend fun getWithAuth(
        url: String,
        headers: Map<String, String> = emptyMap(),
        api42: Api42 // Necesitarás pasar una instancia de Api42
    ): HttpResponse? {
        // 1. Primera llamada con el token actual
        var response = get(url, headers + authHeader())

        // 2. Si el token expiró (401), refrescar y reintentar
        if (response?.status?.value == 401) {
            if (Api42().refreshToken()) { // <- Llama a refreshToken()
                response = get(url, headers + authHeader()) // Nueva llamada con token fresco
            } else {
                throw Exception("No se pudo refrescar el token. Vuelve a iniciar sesión.")
            }
        }
        return response
    }

    // Función auxiliar para construir el header de autenticación
    private fun authHeader(): Map<String, String> {
        return mapOf(
            HttpHeaders.Authorization to "Bearer ${SessionManager.access_token}"
        )
    }

    // Cierra el cliente cuando ya no sea necesario
    fun close() {
        client.close()
    }
}

