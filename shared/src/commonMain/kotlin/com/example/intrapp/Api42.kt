package com.example.intrapp

import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class AuthData(
    val access_token: String,
    val refresh_token: String,
    val userId: Int,
    val userLogin: String,
    val imageUrl: String
)

class Api42() {


    // Credenciales y URLs (desde .env)
    private val client_id: String = BuildKonfig.CLIENT_ID.trim()
    private val redirect_uri: String = BuildKonfig.REDIRECT_URI.trim()
    private val client_secret: String = BuildKonfig.CLIENT_SECRET.trim()
    private val uri: String = "https://api.intra.42.fr/oauth/authorize?client_id=$client_id&redirect_uri=$redirect_uri&response_type=code"

    init {
        println("[API42] client_id: '$client_id'")
        println("[API42] redirect_uri: '$redirect_uri'")
        println("[API42] URI completa: $uri")
    }

    //Devuelve URI de autorizacion de 42
    fun getURI(): String {
        return uri
    }

    // Maneja el callback: intercambia el code por el token y obtiene el perfil
    suspend fun handleCallback(code: String): AuthData {

        println("[API42] Handlecallback(code: $code)")

        //State, strings random para mas seguridad
        val state: String = ""

        try {

            // Paso 1: Intercambiar el code por el token // Rellena accessToken y refreshToken en SessionManager
            val tokens = exchangeCodeForToken(code)

            // Paso 2: Obtener info básica del usuario (id y login y avatar)
            val basicInfo = getBasicUserInfo(tokens.accessToken)

            return AuthData(
                access_token = tokens.accessToken,
                refresh_token = tokens.refreshToken,
                userId = basicInfo.id,
                userLogin = basicInfo.login,
                imageUrl = basicInfo.imageUrl
            )

        } catch (e: Exception) {
            println("[API42] Error en handleCallback: ${e.message}")
            SessionManager.clearSession()
            throw e
        }
    }


    // Intercambia el code por el token de acceso
    private suspend fun exchangeCodeForToken(code: String) :Tokens {
        val url = "https://api.intra.42.fr/oauth/token"
        val body = "grant_type=authorization_code" +
                "&client_id=$client_id" +
                "&client_secret=$client_secret" +
                "&code=$code" +
                "&redirect_uri=$redirect_uri"

        //println("[API42] POST: $url")
        //println("[API42] Body: $body")

        val response = ApiClient().post(url, body)
        println("[API42] exchangeCodeForToken() RESPONSE: ${response?.status?.value}")
        println("[API42] exchangeCodeForToken() JSON: ${response?.bodyAsText()}")

        if (response?.status?.value == 200) {
            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val accessToken = json["access_token"]?.jsonPrimitive?.content
                ?: throw Exception("Access token no encontrado")
            val refreshToken = json["refresh_token"]?.jsonPrimitive?.content
                ?: throw Exception("Refresh token no encontrado")

            // Guardar tokens en Sessionamanager
            SessionManager.access_token = accessToken
            SessionManager.refresh_token = refreshToken

            return Tokens(accessToken, refreshToken)

        } else {
            throw Exception("Código de error ${response?.status?.value}")
        }
    }

    private data class Tokens(val accessToken: String, val refreshToken: String)


    // Función wrapper para Swift que llama a HandleCallback
    @Throws(Throwable::class)
    fun handleCallbackWrapper(code: String) {
        return runBlocking {
            try {
                handleCallback(code)
            } catch (e: Exception) {
                // Limpiar el estado en caso de error (Session manager)
                SessionManager.clearSession()
                throw e
            }
        }
    }

    private suspend fun getBasicUserInfo(accessToken: String): BasicUserInfo {
        val response = ApiClient().getWithAuth(
            url = "https://api.intra.42.fr/v2/me",
            headers = mapOf("Authorization" to "Bearer $accessToken"),
            api42 = this
        ) ?: throw Exception("No se pudo conectar al servidor")

        if (response.status.value != 200) {
            throw Exception("Error obteniendo info de usuario")
        }

        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonObject

        return BasicUserInfo(
            id = json["id"]?.jsonPrimitive?.int?: throw Exception("Campo 'id' no encontrado o inválido"),
            login = json["login"]?.jsonPrimitive?.content ?: throw Exception("Campo 'login' no encontrado o inválido"),
            imageUrl = json["image"]?.jsonObject?.get("link")?.jsonPrimitive?.content ?: "https://cdn.intra.42.fr/users/${json["login"]?.jsonPrimitive?.content}.jpg"

        )
    }

    private data class BasicUserInfo(val id: Int, val login: String, val imageUrl: String)

    suspend fun searchUser(login: String): SelectedUserProfile {
        val url = "https://api.intra.42.fr/v2/users?filter[login]=${login.trim()}"

        println("[API42] SEARCHING FOR LOGIN $login")
        val response = ApiClient().getWithAuth(
            url = url,
            headers = emptyMap(),
            api42 = this
        ) ?: throw Exception("No se pudo conectar al servidor")

        // Primera llamada para obtener el ID del usuario
        val basicProfile = when (response.status.value) {
            200 -> {
                val usersJson = response.bodyAsText()
                println("[API42] JSON FOR LOGIN $login: ${response.bodyAsText()}")
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<List<SelectedUserProfile>>(usersJson)
                    .firstOrNull() ?: throw UserNotFoundException()
            }
            401 -> throw Exception("Token no válido")
            404 -> throw UserNotFoundException()
            else -> throw Exception("Error en la búsqueda: ${response.status.value}")
        }

        // Ahora que tenemos el ID, hacemos la segunda llamada para obtener el perfil detallado
        val userId = basicProfile.id
        val detailUrl = "https://api.intra.42.fr/v2/users/$userId"

        println("[API42] OBTENIENDO PERFIL DETALLADO PARA ID $userId")
        val detailResponse = ApiClient().getWithAuth(
            url = detailUrl,
            headers = emptyMap(),
            api42 = this
        ) ?: throw Exception("No se pudo conectar al servidor")

        if (detailResponse.status.value != 200) {
            throw Exception("Error obteniendo perfil detallado: ${detailResponse.status.value}")
        }

        val detailJson = detailResponse.bodyAsText()
        println("[API42] JSON DETALLADO PARA ID $userId: ${detailJson}")

        // Parsear el perfil detallado
        return Json { ignoreUnknownKeys = true }
            .decodeFromString<SelectedUserProfile>(detailJson)
    }

    // Clase de excepción personalizada
    class UserNotFoundException : Exception("Usuario no encontrado")

    // Wrapper para Swift/Kotlin Native
    @Throws(Throwable::class)
    fun searchUserWrapper(login: String): SelectedUserProfile = runBlocking {
        searchUser(login)
    }

    suspend fun getProjectsForUser(login: String): List<Project> {
        val url = "https://api.intra.42.fr/v2/users/$login/projects_users"
        val response = ApiClient().getWithAuth(
            url = url,
            headers = emptyMap(),
            api42 = this
        ) ?: throw Exception("No se pudo conectar al servidor")

        if (response.status.value != 200) {
            throw Exception("Error obteniendo proyectos: ${response.status.value}")
        }

        return Json { ignoreUnknownKeys = true }
            .decodeFromString<List<Project>>(response.bodyAsText())
    }


    // Función wrapper para Swift que llama a HandleCallback
    @Throws(Throwable::class)
    fun getProjectsWrapper(login: String) {
        return runBlocking {
            try {
                getProjectsForUser(login)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    suspend fun refreshToken(): Boolean {
        val refreshToken = SessionManager.refresh_token ?: return false
        val response = ApiClient().post(
            url = "https://api.intra.42.fr/oauth/token",
            body = "grant_type=refresh_token&client_id=$client_id&client_secret=$client_secret&refresh_token=$refreshToken"
        )
        if (response?.status?.value == 200) {
            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            SessionManager.access_token = json["access_token"]?.jsonPrimitive?.content
            SessionManager.refresh_token = json["refresh_token"]?.jsonPrimitive?.content
            return true
        }
        return false
    }

    @Throws(Exception::class)
    suspend fun refreshTokenWrapper(): Boolean {
        return try {
            refreshToken()
        } catch (e: Exception) {
            false
        }
    }
}



