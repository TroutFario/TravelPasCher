package fr.troubidoo.travelpascher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuth(auth: UserAuthEntity)

    @Transaction
    suspend fun registerUser(user: UserEntity, auth: UserAuthEntity) {
        val userId = insertUser(user)
        insertAuth(auth.copy(userId = userId.toInt()))
    }

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM user_auth WHERE userId = :userId")
    suspend fun getAuthByUserId(userId: Int): UserAuthEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
}
