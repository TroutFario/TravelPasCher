package fr.troubidoo.travelpascher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import fr.troubidoo.travelpascher.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Modèles de données pour l'UI (Directement depuis Firebase)
data class UiPost(
    val userId: Int,
    val username: String,
    val location: String,
    val imageUrl: String,
    val createdAt: Long
)

data class UiStory(
    val userId: Int,
    val username: String,
    val imageUrl: String
)

class FeedViewModel(
    private val postDao: PostDAO,
    private val storyDao: StoryDAO,
    private val userDao: UserDAO
) : ViewModel() {

    private val db = Firebase.firestore

    // On observe directement Firebase via des StateFlow
    private val _posts = MutableStateFlow<List<UiPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<UiStory>>(emptyList())
    val stories = _stories.asStateFlow()

    init {
        listenToFirestorePosts()
        listenToFirestoreStories()
    }

    private fun listenToFirestorePosts() {
        db.collection("posts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        UiPost(
                            userId = doc.getLong("userId")?.toInt() ?: 0,
                            username = doc.getString("username") ?: "Anonyme",
                            location = doc.getString("location") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    }
                    _posts.value = list
                }
            }
    }

    private fun listenToFirestoreStories() {
        db.collection("stories")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        UiStory(
                            userId = doc.getLong("userId")?.toInt() ?: 0,
                            username = doc.getString("username") ?: "Anonyme",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    }
                    _stories.value = list
                }
            }
    }

    fun uploadPost(userId: Int, username: String, location: String) {
        viewModelScope.launch {
            val postData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "location" to location,
                "imageUrl" to "",
                "createdAt" to System.currentTimeMillis()
            )
            try {
                db.collection("posts").add(postData).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Crée un utilisateur dans la collection "users" de Firestore.
     */
    fun registerUser(
        id: Int,
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val userData = hashMapOf(
                "id" to id,
                "username" to username,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                "creationDate" to System.currentTimeMillis()
            )
            try {
                // On utilise l'ID comme nom de document pour plus de clarté
                db.collection("users").document(id.toString()).set(userData).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur inconnue lors de l'inscription")
            }
        }
    }
}
