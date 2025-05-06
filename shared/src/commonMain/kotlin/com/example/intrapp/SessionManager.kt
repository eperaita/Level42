package com.example.intrapp

import com.example.intrapp.UserProfile
import com.example.intrapp.Project

object SessionManager {
    var access_token: String? = null
    var refresh_token: String? = null

    var userProfile: UserProfile? = null

    var projects: List<Project>? = null

    var lastAuthError: String? = null

    fun checkLogIn(): Boolean {
        return access_token != null
    }

    fun clearSession() {
        access_token = null
        refresh_token = null
        userProfile = null
        projects = null
    }
}