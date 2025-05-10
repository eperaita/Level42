package com.example.intrapp.android


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrapp.Api42
import com.example.intrapp.AuthData
import com.example.intrapp.SelectedUserProfile
import com.example.intrapp.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {

    // Estados
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val authData: AuthData) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    sealed class SearchState {
        object Idle : SearchState()
        object Loading : SearchState()
        data class Success(val user: SelectedUserProfile) : SearchState()
        data class Error(val message: String) : SearchState()
    }

    fun handleAuthCallback(code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                println("[VIEWMODEL] Code: $code")

                // Llamar a API42 para obtener tokens y datos básicos
                val authData = Api42().handleCallback(code)

                // Guardar datos en SessionManager
                SessionManager.apply {
                    access_token = authData.access_token
                    refresh_token = authData.refresh_token
                    user_id = authData.userId
                    user_login = authData.userLogin
                    user_image_url = authData.imageUrl
                }
                // Indicar que la autenticación fue exitosa
                _authState.value = AuthState.Success(authData)

                println("[VIEWMODEL]:handlecallback() =  Authdata for : ${SessionManager.user_login}, id: ${SessionManager.user_id}")
                println("[VM]handlecallback() = Image link: ${SessionManager.user_image_url}")
                println("[VM]handlecallback() = Access Token: ${SessionManager.access_token}")
                println("[VM]handlecallback() = Refresh Token: ${SessionManager.refresh_token}")

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error en autenticación", e)
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    // Función para buscar usuarios
    fun searchForUser(login: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val user = Api42().searchUser(login)
                SessionManager.selectedUserProfile = user
                _searchState.value = SearchState.Success(user)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Error buscando usuario")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
        _searchState.value = SearchState.Idle
    }


    // Clase de excepción personalizada
    class UserNotFoundException : Exception("Usuario no encontrado")

    // Funcion para cargar proyectos de un usuario
    suspend fun loadProjectsForUser(login: String) {
        try {
            val projects = Api42().getProjectsForUser(login)
            SessionManager.selectedUserProfile = SessionManager.selectedUserProfile?.copy(
                projects = projects
            )
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading projects for user", e)
            throw e
        }
    }

    // Función para limpiar errores anteriores
    fun clearAuthError() {
        SessionManager.lastAuthError = null
    }

    // Función para limpiar los resultados de búsqueda
    fun clearSearch() {
        SessionManager.selectedUserProfile = null
        _searchState.value = SearchState.Idle
    }


}


/*
viewModelScope.launch: Ejecuta el código en un coroutine ligado al ciclo de vida del ViewModel.
 */