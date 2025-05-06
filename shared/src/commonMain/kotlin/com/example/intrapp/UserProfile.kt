package com.example.intrapp

import kotlinx.serialization.Serializable
import kotlin.jvm.Transient

@Serializable
data class UserProfile(
    val id: Int,
    val login: String,
    val email: String,
    val first_name: String?,
    val last_name: String?,
    var image: Image?,
    val location: String?,
    val wallet: Int,

    var projects: List<Project> = emptyList(), //PROJECTS
    val cursus_users: List<CursusUser> = emptyList() //SKILLS
) {
    @Serializable
    data class Image(
        val link: String,
    )

    // Lógica para obtener el level del cursus "42cursus" (el de id: 21)
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
        val level: Float  // Nivel viene como float (ej. 7.84)
    )

    @Serializable
    data class Cursus(
        val id: Int,
        val name: String
    )
}