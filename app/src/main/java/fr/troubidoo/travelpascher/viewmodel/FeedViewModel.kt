package fr.troubidoo.travelpascher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import fr.troubidoo.travelpascher.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Modèles de données pour l'UI (Directement depuis Firebase)
data class UiPost(
    val id: String,
    val userId: String,
    val username: String,
    val authorProfileImageUrl: String = "",
    val location: String,
    val imageUrl: String,
    val createdAt: Long
)

data class UiStory(
    val userId: String,
    val username: String,
    val imageUrl: String
)

data class UiUser(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val bio: String = "",
    val profileImageUrl: String = ""
)

class FeedViewModel(
    private val postDao: PostDAO,
    private val storyDao: StoryDAO,
    private val userDao: UserDAO
) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<UiUser?>(null)
    val userData = _userData.asStateFlow()

    // On observe directement Firebase via des StateFlow
    private val _posts = MutableStateFlow<List<UiPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<UiStory>>(emptyList())
    val stories = _stories.asStateFlow()

    init {
        listenToFirestorePosts()
        listenToFirestoreStories()
        listenToUserData()
    }

    private fun listenToUserData() {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUser.value = user
                if (user != null) {
                    db.collection("users").document(user.uid)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null && snapshot.exists()) {
                                _userData.value = UiUser(
                                    id = snapshot.getString("id") ?: "",
                                    username = snapshot.getString("username") ?: "",
                                    email = snapshot.getString("email") ?: "",
                                    firstName = snapshot.getString("firstName") ?: "",
                                    lastName = snapshot.getString("lastName") ?: "",
                                    bio = snapshot.getString("bio") ?: "",
                                    profileImageUrl = snapshot.getString("profileImageUrl") ?: ""
                                )
                            }
                        }
                } else {
                    _userData.value = null
                }
            }
        }
    }

    private fun listenToFirestorePosts() {
        db.collection("posts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        UiPost(
                            id = doc.id,
                            userId = doc.get("userId")?.toString() ?: "",
                            username = doc.getString("username") ?: "Anonymous",
                            authorProfileImageUrl = doc.getString("authorProfileImageUrl") ?: "",
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
                            userId = doc.get("userId")?.toString() ?: "",
                            username = doc.getString("username") ?: "Anonymous",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    }
                    _stories.value = list
                }
            }
    }

    fun uploadPost(
        location: String,
        imageUri: Uri? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Vous devez être connecté pour publier")
            return
        }

        viewModelScope.launch {
            try {
                var imageUrl = ""
                
                // 1. Upload de l'image si elle existe
                if (imageUri != null) {
                    val fileName = "posts/${user.uid}_${System.currentTimeMillis()}.jpg"
                    val storageRef = storage.reference.child(fileName)
                    storageRef.putFile(imageUri).await()
                    imageUrl = storageRef.downloadUrl.await().toString()
                }

                // 2. Récupération des infos actuelles de l'utilisateur (pseudo et photo)
                val currentUsername = _userData.value?.username?.ifBlank { null }
                    ?: user.displayName?.ifBlank { null }
                    ?: user.email
                    ?: "Anonymous"
                
                val authorProfileImageUrl = _userData.value?.profileImageUrl ?: ""

                // 3. Création du post dans Firestore
                val postData = hashMapOf(
                    "userId" to user.uid,
                    "username" to currentUsername,
                    "authorProfileImageUrl" to authorProfileImageUrl,
                    "location" to location,
                    "imageUrl" to imageUrl,
                    "createdAt" to System.currentTimeMillis()
                )

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
        profileImageUri: Uri? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Création du compte dans Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    var profileImageUrl = ""
                    
                    // 2. Upload de la photo de profil si elle existe
                    if (profileImageUri != null) {
                        val fileName = "profiles/${firebaseUser.uid}.jpg"
                        val storageRef = storage.reference.child(fileName)
                        storageRef.putFile(profileImageUri).await()
                        profileImageUrl = storageRef.downloadUrl.await().toString()
                    }

                    // 3. Création du profil utilisateur dans Firestore
                    val userData = hashMapOf(
                        "id" to firebaseUser.uid,
                        "username" to username,
                        "email" to email,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "bio" to "",
                        "profileImageUrl" to profileImageUrl,
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
        _userData.value = null
    }

    fun updateUserProfile(firstName: String, lastName: String, bio: String, newProfileImageUri: Uri? = null, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "bio" to bio
                )

                if (newProfileImageUri != null) {
                    // 1. Supprimer l'ancienne image si elle existe
                    val currentImageUrl = _userData.value?.profileImageUrl
                    if (!currentImageUrl.isNullOrEmpty()) {
                        try {
                            storage.getReferenceFromUrl(currentImageUrl).delete().await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // 2. Upload de la nouvelle image
                    val fileName = "profiles/${user.uid}_${System.currentTimeMillis()}.jpg"
                    val storageRef = storage.reference.child(fileName)
                    storageRef.putFile(newProfileImageUri).await()
                    val newImageUrl = storageRef.downloadUrl.await().toString()
                    updates["profileImageUrl"] = newImageUrl
                }

                db.collection("users").document(user.uid).update(updates).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur lors de la mise à jour du profil")
            }
        }
    }

    fun updatePost(postId: String, newLocation: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId).update("location", newLocation).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur lors de la mise à jour")
            }
        }
    }

    fun deletePost(postId: String, imageUrl: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                // 1. Supprimer l'image si elle existe
                if (imageUrl.isNotEmpty()) {
                    try {
                        storage.getReferenceFromUrl(imageUrl).delete().await()
                    } catch (e: Exception) {
                        // On ignore si l'image n'existe plus ou erreur storage
                        e.printStackTrace()
                    }
                }
                // 2. Supprimer le document Firestore
                db.collection("posts").document(postId).delete().await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur lors de la suppression")
            }
        }
    }
}
