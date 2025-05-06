package com.example.intrapp

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.example.intrapp.BuildKonfig


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
    suspend fun handleCallback(code: String) {

        println("[API42] Handlecallback(code: $code)")

        //State, strings random para mas seguridad
        val state: String = ""

        try {

            // Paso 1: Intercambiar el code por el token // Rellena accessToken y refreshToken en SessionManager
            exchangeCodeForToken(code)

            // Paso 2: Obtener el perfil del usuario // // Rellena Userprofile en SessionManager
            getProfile()


        } catch (e: Exception) {
            println("[API42] Error en handleCallback: ${e.message}")
            throw e
        }
    }


    // Intercambia el code por el token de acceso
    private suspend fun exchangeCodeForToken(code: String) {
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

        if (response?.status?.value == 200) {
            val bodyText = response.bodyAsText()
            val jsonObject = Json.parseToJsonElement(bodyText).jsonObject

            // Almacenar tokens en SessionManager
            SessionManager.access_token = jsonObject["access_token"]?.toString()?.replace("\"", "")
            SessionManager.refresh_token =
                jsonObject["refresh_token"]?.toString()?.replace("\"", "")

            println("[API42]exchangeCodeForToken() = Access Token: ${SessionManager.access_token}")
            println("[API42]exchangeCodeForToken() = Refresh Token: ${SessionManager.refresh_token}")

        } else {
            throw Exception("Código de error ${response?.status?.value}")
        }
    }

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

    suspend fun getProfile() {

        val token = SessionManager.access_token ?: throw Exception("Access token no disponible")

        val response: HttpResponse? =
            ApiClient().getWithAuth(
                url = "https://api.intra.42.fr/v2/me",
                headers = emptyMap(),
                api42 = this // Pasar la instancia actual de Api42
            )
        if (response == null || response.status.value != 200) {
            throw Exception("Error: No se pudo obtener el perfil")
        }

        // Parsear el JSON a UserProfile
        val profileJson = response.bodyAsText()
        println("[API42] getProfile() = USER PROFILE JSON: $profileJson")

        //Parsear a modelo de datos tipo UseProfile y Almacenar en SessionManager

        val userProfile = Json {
            ignoreUnknownKeys = true
        }.decodeFromString<UserProfile>(profileJson) // Ignora las claves que no están en el modelo
        SessionManager.userProfile = userProfile

        //println("[API42] getProfile() : USER PROFILE MODEL: ${SessionManager.userProfile?.id}, ${SessionManager.userProfile?.login}, ${SessionManager.userProfile?.email}, ${SessionManager.userProfile?.location}, ${SessionManager.userProfile?.wallet}\")")

    }

    // Función wrapper para Swift que llama a getProfile
    @Throws(Throwable::class)
    fun getProfileWrapper() {
        return runBlocking {
            try {
                getProfile()
            } catch (e: Exception) {
                // Limpiar el perfil en caso de error
                SessionManager.userProfile = null
                throw e
            }
        }
    }

    suspend fun getProjects() {

        try {
            // Verificar que el accessToken y el userProfile estén disponibles
            val token = SessionManager.access_token ?: throw Exception("Access token no disponible")
            val userId = SessionManager.userProfile?.id ?: throw Exception("User ID no disponible")

            // Hacer la solicitud a la API
            val response: HttpResponse? = ApiClient().getWithAuth(
                url = "https://api.intra.42.fr/v2/users/$userId/projects_users",
                headers = emptyMap(),
                api42 = this // Pasar la instancia actual de Api42
            )

            // Verificar la respuesta
            if (response == null || response.status.value != 200) {
                throw Exception("Error: No se pudieron obtener los proyectos (código de estado ${response?.status?.value})")
            }

            // Parsear el JSON a List<Project>
            val projectsJson = response.bodyAsText()
            println("[API42] getprojects() PROJECTS JSON: $projectsJson")
            // Extraer solo los campos necesarios
            val projects =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<Project>>(projectsJson)

            // Almacenar los proyectos en SessionManager - Userprofile - Projects
            SessionManager.userProfile?.projects = projects

            println("[API42] Proyectos cargados: ${projects.size}")

        } catch (e: Exception) {
            // Limpiar los proyectos en caso de error
            SessionManager.userProfile?.projects = emptyList()

            println("[API42] Proyectos no cargados : ${e.message}")
            throw Exception("Error en getProjects: ${e.message}", e)
        }
    }

    // Función wrapper para Swift que llama a HandleCallback
    @Throws(Throwable::class)
    fun getProjectsWrapper() {
        return runBlocking {
            try {
                getProjects()
            } catch (e: Exception) {
                SessionManager.userProfile?.projects =
                    emptyList() //Limpiar projects en caso de error
                throw e
            }
        }
    }

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



