package fr.troubidoo.travelpascher.ui.model

data class User(
    val id: Int,
    val username: String,
    val creationDate: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePictureRes: Int? = null
)
