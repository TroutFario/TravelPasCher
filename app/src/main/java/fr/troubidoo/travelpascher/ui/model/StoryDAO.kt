package fr.troubidoo.travelpascher.ui.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDAO {
    @Query("SELECT * FROM stories ORDER BY creationDate DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert
    suspend fun insertStory(story: StoryEntity)
}