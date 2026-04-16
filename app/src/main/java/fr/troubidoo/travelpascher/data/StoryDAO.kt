package fr.troubidoo.travelpascher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDAO {
    @Transaction
    @Query("SELECT * FROM stories ORDER BY creationDate DESC")
    fun getAllStoriesWithUser(): Flow<List<StoryWithUser>>

    @Query("SELECT * FROM stories ORDER BY creationDate DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert
    suspend fun insertStory(story: StoryEntity)

    @Query("SELECT COUNT(*) FROM stories")
    suspend fun getCount(): Int
}
