package fr.troubidoo.travelpascher.data

import androidx.room.Embedded
import androidx.room.Relation

data class PostWithUser(
    @Embedded val post: PostEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)

data class StoryWithUser(
    @Embedded val story: StoryEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)
