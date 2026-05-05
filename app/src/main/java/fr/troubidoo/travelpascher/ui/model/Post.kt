package fr.troubidoo.travelpascher.ui.model

data class Post(
    val username: String,
    val location: String,
    val imageUrl: String,
    val createdAt: Long
)