package com.example.intrapp

object SessionManager {
    var access_token: String? = null
    var refresh_token: String? = null

    // Datos del usuario autenticado
    var user_id: Int? = null
    var user_login: String? = null
    var user_image_url: String? = null

    // Datos del usuario buscado (incluye projects)
    var selectedUserProfile: SelectedUserProfile? = null

    // Manejo de errores
    var lastAuthError: String? = null

    fun checkLogIn(): Boolean {
        return access_token != null
    }

    fun clearSession() {
        access_token = null
        refresh_token = null
        user_id = null
        user_login = null
        selectedUserProfile = null
    }
}