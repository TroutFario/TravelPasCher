package fr.troubidoo.travelpascher.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val creationDate: Long
)

@Entity(
    tableName = "user_auth",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAuthEntity(
    @PrimaryKey val userId: Int,
    val passwordHash: String,
    val salt: String, // Le "sel" pour renforcer le hashage
    val lastLogin: Long
)
