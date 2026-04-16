package fr.troubidoo.travelpascher.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // Lien vers l'utilisateur
    val location: String,
    val imageRes: Int,
    val createdAt: Long
)
