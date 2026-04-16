package fr.troubidoo.travelpascher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDAO {

    @Transaction
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPostsWithUser(): Flow<List<PostWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("DELETE FROM posts")
    suspend fun deleteAll()
}