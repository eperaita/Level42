package com.example.intrapp

import kotlinx.serialization.Serializable

@Serializable
data class SelectedUserProfile(
    val id: Int,
    val login: String,
    val email: String,
    val first_name: String?,
    val last_name: String?,
    var image: Image?,
    val location: String?,
    val wallet: Int,
    var projects: List<Project> = emptyList(),
    val cursus_users: List<CursusUser> = emptyList()
) {
    @Serializable
    data class Image(
        val link: String,
    )

    // Misma lógica para obtener el level del cursus principal
    val level: Double? = cursus_users
        .firstOrNull { it.cursus.id == 21 }
        ?.level

    @Serializable
    data class CursusUser(
        val skills: List<Skill>?,
        val cursus: Cursus,
        val level: Double?
    )

    @Serializable
    data class Skill(
        val id: Int,
        val name: String,
        val level: Float
    )

    @Serializable
    data class Cursus(
        val id: Int,
        val name: String
    )


}