package fr.troubidoo.travelpascher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FeedViewModel(
    private val postDao: PostDAO,
    private val storyDao: StoryDAO,
    private val userDao: UserDAO
) : ViewModel() {

    // On utilise maintenant les versions "WithUser"
    val posts = postDao.getAllPostsWithUser()
    val stories = storyDao.getAllStoriesWithUser()

    init {
        seedDatabase()
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            val currentPosts = postDao.getAllPostsWithUser().first()
            if (currentPosts.isEmpty()) {
                // 1. Créer des utilisateurs
                val user1 = UserEntity(1, "Bernard", "test@test.com", "John", "Doe", System.currentTimeMillis())
                val auth1 = UserAuthEntity(1, "pass", "salt", System.currentTimeMillis())
                userDao.registerUser(user1, auth1)

                val user2 = UserEntity(2, "Alice", "alice@test.com", "Alice", "Wonder", System.currentTimeMillis())
                val auth2 = UserAuthEntity(2, "pass", "salt", System.currentTimeMillis())
                userDao.registerUser(user2, auth2)

                val user3 = UserEntity(2, "Minet", "minet@test.com", "Bernard", "Wonder", System.currentTimeMillis())
                val auth3 = UserAuthEntity(2, "pass", "salt", System.currentTimeMillis())
                userDao.registerUser(user3, auth3)
                
                // 2. Insérer des posts liés par ID (plus par String !)
                postDao.insertPost(PostEntity(userId = 1, location = "Paris", imageRes = R.drawable.chevoul, createdAt = System.currentTimeMillis()))
                postDao.insertPost(PostEntity(userId = 2, location = "Montarnaud", imageRes = R.drawable.chevoul, createdAt = System.currentTimeMillis() - 3600000))
                postDao.insertPost(PostEntity(userId = 1, location = "Montpellier", imageRes = R.drawable.chevoul, createdAt = System.currentTimeMillis() - 3600000))
                postDao.insertPost(PostEntity(userId = 3, location = "Saint-Paul-et-Valemalle", imageRes = R.drawable.chevoul, createdAt = System.currentTimeMillis() - 3600000))

                // 3. Insérer des stories liées par ID
                if (storyDao.getCount() == 0) {
                    storyDao.insertStory(StoryEntity(userId = 1, imageRes = R.drawable.chevoul, creationDate = System.currentTimeMillis()))
                    storyDao.insertStory(StoryEntity(userId = 2, imageRes = R.drawable.chevoul, creationDate = System.currentTimeMillis() - 10000))
                }
            }
        }
    }
}
