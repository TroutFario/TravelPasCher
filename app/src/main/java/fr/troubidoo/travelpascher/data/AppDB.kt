package fr.troubidoo.travelpascher.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PostEntity::class, UserEntity::class, UserAuthEntity::class, StoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDAO
    abstract fun postDao(): PostDAO
    abstract fun userDao(): UserDAO
}
