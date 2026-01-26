package com.example.myapplication.Client

import com.example.myapplication.R

data class UserFalso(
    val id: String,
    val username: String,
    val name: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: Any?,
    val direccionCasa: String? = null,
    val direccionTrabajo: String? = null,
    val ciudad: String,
    val favoriteProviderIds: List<String> = emptyList()
)

object UserSampleDataFalso {
    internal val users = mutableListOf(
        UserFalso(
            id = "user1",
            username = "maxinanterne",
            name = "Maximiliano",
            lastName = "Nanterne",
            email = "maxi.nanterne@example.com",
            profileImageUrl = R.drawable.maverickprofile,
            direccionCasa = "Av. Siempre Viva 742",
            direccionTrabajo = "Planta Nuclear de Springfield",
            ciudad = "Paraná",
            favoriteProviderIds = listOf("1", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22")
        ),
        UserFalso(
            id = "user2",
            username = "juanperez",
            name = "Juan",
            lastName = "Perez",
            email = "juan.perez@example.com",
            profileImageUrl = "https://picsum.photos/seed/user2/200/200",
            direccionCasa = "Calle Falsa 123",
            ciudad = "Santa Fe",
            favoriteProviderIds = listOf("3", "5", "7")
        )
    )
    fun findUserByUsername(username: String): UserFalso? {
        return users.find { it.username.equals(username, ignoreCase = true) }
    }
}
