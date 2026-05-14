package fr.troubidoo.travelpascher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
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
    private val auth = Firebase.auth

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

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

    fun uploadPost(
        location: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Vous devez être connecté pour publier")
            return
        }

        viewModelScope.launch {
            val postData = hashMapOf(
                "userId" to user.uid,
                "username" to (user.displayName ?: user.email ?: "Anonyme"),
                "location" to location,
                "imageUrl" to "",
                "createdAt" to System.currentTimeMillis()
            )
            try {
                db.collection("posts").add(postData).await()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Erreur inconnue lors de l'envoi")
            }
        }
    }

    /**
     * Connecte un utilisateur avec Firebase Auth.
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = authResult.user
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("AUTH_ERROR", "Login failed for email: $email", e)
                val message = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Identifiants invalides (email ou mot de passe incorrect)."
                    is FirebaseAuthInvalidUserException -> "Utilisateur non trouvé."
                    else -> e.message ?: "Erreur de connexion"
                }
                onError(message)
            }
        }
    }

    /**
     * Crée un utilisateur avec Firebase Auth puis stocke son profil dans Firestore.
     */
    fun registerUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Création du compte dans Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // 2. Création du profil utilisateur dans Firestore
                    val userData = hashMapOf(
                        "id" to firebaseUser.uid,
                        "username" to username,
                        "email" to email,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "creationDate" to System.currentTimeMillis()
                    )
                    db.collection("users").document(firebaseUser.uid).set(userData).await()
                    _currentUser.value = firebaseUser
                    onSuccess()
                } else {
                    onError("Échec de la création de l'utilisateur")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Erreur inconnue lors de l'inscription")
            }
        }
    }

    /**
     * Déconnecte l'utilisateur.
     */
    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }
}
