package fr.troubidoo.travelpascher.data

import fr.troubidoo.travelpascher.ui.model.Post

fun PostEntity.toPost(username: String): Post {
    return Post(
        username = username,
        location = location,
        imageRes = imageRes,
        createdAt = createdAt
    )
}

fun Post.toEntity(userId: Int): PostEntity {
    return PostEntity(
        id = 0,
        userId = userId,
        location = location,
        imageRes = imageRes,
        createdAt = createdAt
    )
}
