package fr.troubidoo.travelpascher.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Math.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class UiPost(
    val id: String,
    val userId: String,
    val username: String,
    val authorProfileImageUrl: String = "",
    val location: String,
    val description: String = "",
    val imageUrl: String,
    val createdAt: Long,
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    companion object {
        fun fromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): UiPost? {
            return try {
                UiPost(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Anonymous",
                    authorProfileImageUrl = doc.getString("authorProfileImageUrl") ?: "",
                    location = doc.getString("location") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    likedBy = (doc.get("likedBy") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList(),
                    commentCount = doc.getLong("commentCount")?.toInt() ?: 0,
                    latitude = (doc.get("latitude") as? Number)?.toDouble(),
                    longitude = (doc.get("longitude") as? Number)?.toDouble()
                )
            } catch (e: Exception) {
                android.util.Log.e("FACTORY", "Error parsing UiPost: ${doc.id}", e)
                null
            }
        }
    }
}

data class UiStory(
    val userId: String, val username: String, val imageUrl: String
) {
    companion object {
        fun fromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): UiStory? {
            return try {
                UiStory(
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Anonymous",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            } catch (e: Exception) {
                android.util.Log.e("FACTORY", "Error parsing UiStory: ${doc.id}", e)
                null
            }
        }
    }
}

data class UiUser(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val bio: String = "",
    val preferredCategories: List<String> = emptyList(),
    val profileImageUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val savedPosts: List<String> = emptyList()
) {
    companion object {
        fun fromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): UiUser? {
            return try {
                UiUser(
                    id = doc.getString("id") ?: doc.id,
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    firstName = doc.getString("firstName") ?: "",
                    lastName = doc.getString("lastName") ?: "",
                    bio = doc.getString("bio") ?: "",
                    preferredCategories = (doc.get("preferredCategories") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList(),
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    followers = (doc.get("followers") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList(),
                    following = (doc.get("following") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList(),
                    savedPosts = (doc.get("savedPosts") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList()
                )
            } catch (e: Exception) {
                android.util.Log.e("FACTORY", "Error parsing UiUser: ${doc.id}", e)
                null
            }
        }
    }
}

data class UiComment(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String = "",
    val text: String,
    val createdAt: Long
) {
    companion object {
        fun fromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): UiComment? {
            return try {
                UiComment(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Anonymous",
                    userProfileImageUrl = doc.getString("userProfileImageUrl") ?: "",
                    text = doc.getString("text") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } catch (e: Exception) {
                android.util.Log.e("FACTORY", "Error parsing UiComment: ${doc.id}", e)
                null
            }
        }
    }
}

data class UiItinerary(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val destination: String,
    val createdAt: Long,
    val activities: List<UiActivity> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startDate: String = "",
    val endDate: String = ""
) {
    companion object {
        fun fromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): UiItinerary? {
            return try {
                @Suppress("UNCHECKED_CAST") val activitiesData =
                    doc.get("activities") as? List<Map<String, Any>> ?: emptyList()
                val activities = activitiesData.mapNotNull { UiActivity.fromMap(it) }

                UiItinerary(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    destination = doc.getString("destination") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    activities = activities,
                    latitude = (doc.get("latitude") as? Number)?.toDouble(),
                    longitude = (doc.get("longitude") as? Number)?.toDouble(),
                    startDate = doc.getString("startDate") ?: "",
                    endDate = doc.getString("endDate") ?: ""
                )
            } catch (e: Exception) {
                android.util.Log.e("FACTORY", "Error parsing UiItinerary: ${doc.id}", e)
                null
            }
        }
    }
}

data class UiActivity(
    val id: String = "",
    val name: String,
    val description: String,
    val location: String,
    val category: String,
    val rating: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val price: Double? = null
) {
    companion object {
        fun fromMap(map: Map<String, Any>): UiActivity? {
            return try {
                UiActivity(
                    id = map["id"]?.toString() ?: "",
                    name = map["name"]?.toString() ?: "",
                    description = map["description"]?.toString() ?: "",
                    location = map["location"]?.toString() ?: "",
                    category = map["category"]?.toString() ?: "",
                    rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                    latitude = (map["latitude"] as? Number)?.toDouble(),
                    longitude = (map["longitude"] as? Number)?.toDouble(),
                    price = (map["price"] as? Number)?.toDouble()
                )
            } catch (e: Exception) {
                android.util.Log.e("FACTORY", "Error parsing UiActivity from map", e)
                null
            }
        }
    }
}

class FeedViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<UiUser?>(null)
    val userData = _userData.asStateFlow()

    private val _posts = MutableStateFlow<List<UiPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<UiStory>>(emptyList())
    val stories = _stories.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<UiUser?>(null)
    val selectedUserProfile = _selectedUserProfile.asStateFlow()

    private val _selectedUserPosts = MutableStateFlow<List<UiPost>>(emptyList())
    val selectedUserPosts = _selectedUserPosts.asStateFlow()

    private val _selectedUserItineraries = MutableStateFlow<List<UiItinerary>>(emptyList())
    val selectedUserItineraries = _selectedUserItineraries.asStateFlow()

    private val _currentPostComments = MutableStateFlow<List<UiComment>>(emptyList())
    val currentPostComments = _currentPostComments.asStateFlow()

    private val _itineraries = MutableStateFlow<List<UiItinerary>>(emptyList())
    val itineraries = _itineraries.asStateFlow()

    private val _searchResultsUsers = MutableStateFlow<List<UiUser>>(emptyList())
    val searchResultsUsers = _searchResultsUsers.asStateFlow()

    private val _searchResultsPosts = MutableStateFlow<List<UiPost>>(emptyList())
    val searchResultsPosts = _searchResultsPosts.asStateFlow()

    private val _searchResultsItineraries = MutableStateFlow<List<UiItinerary>>(emptyList())
    val searchResultsItineraries = _searchResultsItineraries.asStateFlow()

    private val _globalActivities = MutableStateFlow<List<UiActivity>>(emptyList())
    val globalActivities = _globalActivities.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable = _isUsernameAvailable.asStateFlow()

    private var commentsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var itinerariesListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        listenToFirestorePosts()
        listenToFirestoreStories()
        listenToUserData()
        listenToItineraries()
    }

    private fun listenToUserData() {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUser.value = user

                listenToItineraries()

                if (user != null) {
                    db.collection("users").document(user.uid).addSnapshotListener { snapshot, e ->
                        if (e != null) return@addSnapshotListener
                        if (snapshot != null && snapshot.exists()) {
                            _userData.value = UiUser.fromSnapshot(snapshot)
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
                        UiPost.fromSnapshot(doc)
                    }
                    _posts.value = list
                }
            }
    }

    private fun listenToFirestoreStories() {
        db.collection("stories").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            if (snapshots != null) {
                val list = snapshots.documents.mapNotNull { doc ->
                    UiStory.fromSnapshot(doc)
                }
                _stories.value = list
            }
        }
    }

    private fun String.normalize(): String {
        return this.lowercase().replace("\\s".toRegex(), "").trim()
    }

    fun uploadPost(
        location: String,
        description: String,
        imageUri: Uri? = null,
        lat: Double? = null,
        lon: Double? = null,
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

                if (imageUri != null) {
                    val fileName = "posts/${user.uid}_${System.currentTimeMillis()}.jpg"
                    val storageRef = storage.reference.child(fileName)
                    storageRef.putFile(imageUri).await()
                    imageUrl = storageRef.downloadUrl.await().toString()
                }

                val currentUsername = _userData.value?.username?.ifBlank { null }
                    ?: user.displayName?.ifBlank { null } ?: user.email ?: "Anonymous"

                val authorProfileImageUrl = _userData.value?.profileImageUrl ?: ""

                val postData = hashMapOf(
                    "userId" to user.uid,
                    "username" to currentUsername,
                    "authorProfileImageUrl" to authorProfileImageUrl,
                    "location" to location,
                    "locationLowercase" to location.normalize(),
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "createdAt" to System.currentTimeMillis(),
                    "likedBy" to arrayListOf<String>(),
                    "latitude" to lat,
                    "longitude" to lon
                )

                db.collection("posts").add(postData).await()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Erreur inconnue lors de l'envoi")
            }
        }
    }


    fun loginUser(
        email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit
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
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    var profileImageUrl = ""

                    if (profileImageUri != null) {
                        val fileName = "profiles/${firebaseUser.uid}.jpg"
                        val storageRef = storage.reference.child(fileName)
                        storageRef.putFile(profileImageUri).await()
                        profileImageUrl = storageRef.downloadUrl.await().toString()
                    }

                    val userData = hashMapOf(
                        "id" to firebaseUser.uid,
                        "username" to username,
                        "usernameLowercase" to username.normalize(), // Champ de recherche
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

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userData.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(1000)
            _isRefreshing.value = false
        }
    }

    fun updateUserProfile(
        username: String,
        firstName: String,
        lastName: String,
        bio: String,
        preferredCategories: List<String> = emptyList(),
        newProfileImageUri: Uri? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val updates = hashMapOf(
                    "username" to username,
                    "usernameLowercase" to username.normalize(), // Champ de recherche
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "bio" to bio,
                    "preferredCategories" to preferredCategories
                )

                if (newProfileImageUri != null) {
                    val currentImageUrl = _userData.value?.profileImageUrl
                    if (!currentImageUrl.isNullOrEmpty()) {
                        try {
                            storage.getReferenceFromUrl(currentImageUrl).delete().await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

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

    fun toggleLike(postId: String) {
        val user = auth.currentUser ?: return
        val posts = _posts.value
        val post = posts.find { it.id == postId } ?: return
        val isLiked = post.likedBy.contains(user.uid)

        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                if (isLiked) {
                    postRef.update("likedBy", FieldValue.arrayRemove(user.uid)).await()
                } else {
                    postRef.update("likedBy", FieldValue.arrayUnion(user.uid)).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleBookmark(postId: String) {
        val user = auth.currentUser ?: return
        val isBookmarked = _userData.value?.savedPosts?.contains(postId) == true

        viewModelScope.launch {
            try {
                val userRef = db.collection("users").document(user.uid)
                if (isBookmarked) {
                    userRef.update("savedPosts", FieldValue.arrayRemove(postId)).await()
                } else {
                    userRef.update("savedPosts", FieldValue.arrayUnion(postId)).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResultsUsers.value = emptyList()
            return
        }
        val normalizedQuery = query.normalize()
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereGreaterThanOrEqualTo("usernameLowercase", normalizedQuery)
                    .whereLessThanOrEqualTo("usernameLowercase", normalizedQuery + "\uf8ff")
                    .get().await()
                _searchResultsUsers.value = snapshot.documents.mapNotNull { UiUser.fromSnapshot(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchPosts(query: String) {
        if (query.isBlank()) {
            _searchResultsPosts.value = emptyList()
            return
        }
        val normalizedQuery = query.normalize()
        viewModelScope.launch {
            try {
                val snapshot = db.collection("posts")
                    .whereGreaterThanOrEqualTo("locationLowercase", normalizedQuery)
                    .whereLessThanOrEqualTo("locationLowercase", normalizedQuery + "\uf8ff")
                    .get().await()
                _searchResultsPosts.value = snapshot.documents.mapNotNull { UiPost.fromSnapshot(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchItinerariesInGlobal(query: String) {
        if (query.isBlank()) {
            _searchResultsItineraries.value = emptyList()
            return
        }
        val normalizedQuery = query.normalize()
        viewModelScope.launch {
            try {
                val snapshot = db.collection("itineraries")
                    .whereGreaterThanOrEqualTo("destinationLowercase", normalizedQuery)
                    .whereLessThanOrEqualTo("destinationLowercase", normalizedQuery + "\uf8ff")
                    .get().await()
                _searchResultsItineraries.value = snapshot.documents.mapNotNull { UiItinerary.fromSnapshot(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePost(
        postId: String,
        newLocation: String,
        newDescription: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "location" to newLocation,
                    "locationLowercase" to newLocation.normalize(),
                    "description" to newDescription
                )
                db.collection("posts").document(postId).update(updates).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erreur lors de la mise à jour")
            }
        }
    }

    fun deletePost(
        postId: String, imageUrl: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId).delete().await()

                if (imageUrl.isNotEmpty() && imageUrl.startsWith("https://firebasestorage")) {
                    try {
                        storage.getReferenceFromUrl(imageUrl).delete().await()
                    } catch (e: Exception) {
                        android.util.Log.e("DELETE_POST", "Failed to delete storage file", e)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("DELETE_POST", "Failed to delete firestore document", e)
                onError(e.message ?: "Erreur lors de la suppression")
            }
        }
    }

    fun listenToComments(postId: String) {
        commentsListener?.remove()
        commentsListener = db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        UiComment.fromSnapshot(doc)
                    }
                    _currentPostComments.value = list
                }
            }
    }

    fun stopListeningToComments() {
        commentsListener?.remove()
        _currentPostComments.value = emptyList()
    }

    fun addComment(postId: String, text: String) {
        val user = auth.currentUser ?: return
        val username =
            _userData.value?.username?.ifBlank { null } ?: user.displayName ?: "Anonymous"
        val profileUrl = _userData.value?.profileImageUrl ?: ""

        val commentData = hashMapOf(
            "userId" to user.uid,
            "username" to username,
            "userProfileImageUrl" to profileUrl,
            "text" to text,
            "createdAt" to System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                db.runTransaction { transaction ->
                    transaction.set(postRef.collection("comments").document(), commentData)
                    transaction.update(postRef, "commentCount", FieldValue.increment(1))
                }.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateComment(postId: String, commentId: String, newText: String) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId).collection("comments").document(commentId)
                    .update("text", newText).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                db.runTransaction { transaction ->
                    transaction.delete(postRef.collection("comments").document(commentId))
                    transaction.update(postRef, "commentCount", FieldValue.increment(-1))
                }.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    _selectedUserProfile.value = UiUser.fromSnapshot(userDoc)
                }

                val postsSnapshot = db.collection("posts").whereEqualTo("userId", userId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get().await()

                _selectedUserPosts.value = postsSnapshot.documents.mapNotNull { doc ->
                    UiPost.fromSnapshot(doc)
                }

                val itinerariesSnapshot = db.collection("itineraries")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get().await()

                _selectedUserItineraries.value = itinerariesSnapshot.documents.mapNotNull { doc ->
                    UiItinerary.fromSnapshot(doc)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSelectedUserProfile() {
        _selectedUserProfile.value = null
        _selectedUserPosts.value = emptyList()
        _selectedUserItineraries.value = emptyList()
    }

    fun checkUsernameAvailability(username: String) {
        if (username.length < 3) {
            _isUsernameAvailable.value = null
            return
        }

        if (username == _userData.value?.username) {
            _isUsernameAvailable.value = true
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("username", username)
                    .get().await()
                _isUsernameAvailable.value = snapshot.isEmpty
            } catch (e: Exception) {
                _isUsernameAvailable.value = null
            }
        }
    }

    fun resetUsernameAvailability() {
        _isUsernameAvailable.value = null
    }

    fun toggleFollow(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == targetUserId) return

        viewModelScope.launch {
            try {
                val currentUserRef = db.collection("users").document(currentUserId)
                val targetUserRef = db.collection("users").document(targetUserId)

                val targetUserDoc = targetUserRef.get().await()
                val followers =
                    @Suppress("UNCHECKED_CAST") (targetUserDoc.get("followers") as? List<String>
                        ?: emptyList())
                val isFollowing = followers.contains(currentUserId)

                db.runTransaction { transaction ->
                    if (isFollowing) {
                        transaction.update(
                            currentUserRef, "following", FieldValue.arrayRemove(targetUserId)
                        )
                        transaction.update(
                            targetUserRef, "followers", FieldValue.arrayRemove(currentUserId)
                        )
                    } else {
                        transaction.update(
                            currentUserRef, "following", FieldValue.arrayUnion(targetUserId)
                        )
                        transaction.update(
                            targetUserRef, "followers", FieldValue.arrayUnion(currentUserId)
                        )
                    }
                }.await()

                if (_selectedUserProfile.value?.id == targetUserId) {
                    fetchUserProfile(targetUserId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun listenToItineraries() {
        itinerariesListener?.remove()

        itinerariesListener = db.collection("itineraries")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("FIRESTORE", "Listen itineraries failed", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        UiItinerary.fromSnapshot(doc)
                    }
                    _itineraries.value = list
                }
            }
    }

    fun searchItineraries(query: String) {
        if (query.isBlank()) {
            listenToItineraries()
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("itineraries")
                    .whereGreaterThanOrEqualTo("destination", query)
                    .whereLessThanOrEqualTo("destination", query + "\uf8ff")
                    .get().await()

                _itineraries.value = snapshot.documents.mapNotNull { doc ->
                    UiItinerary.fromSnapshot(doc)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addItinerary(
        title: String,
        description: String,
        destination: String,
        activities: List<UiActivity>,
        lat: Double? = null,
        lon: Double? = null,
        startDate: String = "",
        endDate: String = ""
    ) {
        val user = auth.currentUser ?: return
        val itineraryData = hashMapOf(
            "userId" to user.uid,
            "title" to title,
            "description" to description,
            "destination" to destination,
            "destinationLowercase" to destination.normalize(), // Champ de recherche
            "createdAt" to System.currentTimeMillis(),
            "latitude" to lat,
            "longitude" to lon,
            "startDate" to startDate,
            "endDate" to endDate,
            "activities" to activities.map {
                hashMapOf(
                    "id" to it.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                    "name" to it.name,
                    "description" to it.description,
                    "location" to it.location,
                    "category" to it.category,
                    "rating" to it.rating,
                    "latitude" to it.latitude,
                    "longitude" to it.longitude,
                    "price" to it.price
                )
            })

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                db.collection("itineraries").add(itineraryData).await()
                _isRefreshing.value = false
            } catch (e: Exception) {
                _isRefreshing.value = false
                e.printStackTrace()
            }
        }
    }

    fun deleteItinerary(itineraryId: String) {
        viewModelScope.launch {
            try {
                db.collection("itineraries").document(itineraryId).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addActivityToItinerary(itineraryId: String, activity: UiActivity) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val activityData = hashMapOf(
                    "id" to activity.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                    "name" to activity.name,
                    "description" to activity.description,
                    "location" to activity.location,
                    "category" to activity.category,
                    "rating" to activity.rating,
                    "latitude" to activity.latitude,
                    "longitude" to activity.longitude,
                    "price" to activity.price
                )
                db.collection("itineraries").document(itineraryId)
                    .update("activities", FieldValue.arrayUnion(activityData)).await()
                _isRefreshing.value = false
            } catch (e: Exception) {
                _isRefreshing.value = false
                e.printStackTrace()
            }
        }
    }

    fun deleteActivityFromItinerary(itineraryId: String, activity: UiActivity) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val activityData = hashMapOf(
                    "id" to activity.id,
                    "name" to activity.name,
                    "description" to activity.description,
                    "location" to activity.location,
                    "category" to activity.category,
                    "rating" to activity.rating,
                    "latitude" to activity.latitude,
                    "longitude" to activity.longitude,
                    "price" to activity.price
                )
                db.collection("itineraries").document(itineraryId)
                    .update("activities", FieldValue.arrayRemove(activityData)).await()
                _isRefreshing.value = false
            } catch (e: Exception) {
                _isRefreshing.value = false
                e.printStackTrace()
            }
        }
    }

    fun searchActivitiesNearby(lat: Double, lon: Double, type: String? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val results = fetchActivitiesFromGoogle(lat, lon, type)
            if (results.isNotEmpty()) {
                _globalActivities.value = results
            }
            _isRefreshing.value = false
        }
    }

    fun generateSmartItinerary(
        title: String,
        description: String,
        destination: String,
        lat: Double?,
        lon: Double?,
        startDate: String,
        endDate: String
    ) {
        if (lat == null || lon == null) return

        viewModelScope.launch {
            try {
                _isRefreshing.value = true

                val durationInDays = calculateDurationInDays(startDate, endDate)
                val maxTotalCapacity = durationInDays * 3

                val nearbyActivities = fetchActivitiesFromGoogle(lat, lon)

                val userPrefs = _userData.value?.preferredCategories ?: emptyList()
                val scoredActivities = nearbyActivities.map { act ->
                    val dist =
                        calculateDistance(lat, lon, act.latitude ?: 0.0, act.longitude ?: 0.0)
                    val isPreferred =
                        userPrefs.any { pref -> act.category.contains(pref, ignoreCase = true) }
                    val score = dist * (if (isPreferred) 0.5 else 1.0)
                    Triple(act, dist, score)
                }.sortedBy { it.third }

                val selectedActivities = mutableListOf<UiActivity>()
                var currentUsedCapacity = 0
                var museumCount = 0

                for (triple in scoredActivities) {
                    val activity = triple.first
                    val cost = when (activity.category.lowercase()) {
                        "museum", "art_gallery", "musée" -> 2
                        else -> 1
                    }

                    val isMuseum = activity.category.lowercase()
                        .contains("museum") || activity.category.lowercase().contains("musée")
                    val canAddMuseum = !isMuseum || museumCount < durationInDays

                    if (currentUsedCapacity + cost <= maxTotalCapacity && canAddMuseum) {
                        selectedActivities.add(activity)
                        currentUsedCapacity += cost
                        if (cost == 2) museumCount++
                    }

                    if (currentUsedCapacity >= maxTotalCapacity) break
                }

                addItinerary(
                    title,
                    description,
                    destination,
                    selectedActivities,
                    lat,
                    lon,
                    startDate,
                    endDate
                )
                _isRefreshing.value = false
            } catch (e: Exception) {
                android.util.Log.e("PLACES", "Error generating itinerary", e)
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun fetchActivitiesFromGoogle(
        lat: Double, lon: Double, specificType: String? = null
    ): List<UiActivity> {
        val context = Firebase.auth.app.applicationContext
        val placesClient = com.google.android.libraries.places.api.Places.createClient(context)

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.TYPES,
            Place.Field.RATING,
            Place.Field.LOCATION,
            Place.Field.PRICE_LEVEL
        )

        val center = com.google.android.gms.maps.model.LatLng(lat, lon)
        val circle = CircularBounds.newInstance(center, 10000.0)

        val allResults = mutableListOf<UiActivity>()
        val types = if (specificType != null) listOf(specificType)
        else listOf(
            "tourist_attraction", "museum", "park", "restaurant", "cafe", "lodging", "shopping_mall"
        )

        for (type in types) {
            val request =
                SearchNearbyRequest.builder(circle, placeFields).setIncludedTypes(listOf(type))
                    .setMaxResultCount(20).build()

            try {
                val response = placesClient.searchNearby(request).await()
                val activities = response.places.map { p ->
                    UiActivity(
                        id = p.id ?: "",
                        name = p.displayName ?: "Lieu inconnu",
                        description = "Recommandé par Google",
                        location = "",
                        category = p.placeTypes?.firstOrNull() ?: type,
                        rating = p.rating ?: 0.0,
                        latitude = p.location?.latitude,
                        longitude = p.location?.longitude,
                        price = (p.priceLevel?.toDouble() ?: 0.0) * 10.0
                    )
                }
                allResults.addAll(activities)
            } catch (e: Exception) {
                android.util.Log.e("PLACES", "Search failed for $type", e)
            }
        }
        return allResults.distinctBy { it.id }
    }

    private fun calculateDurationInDays(start: String, end: String): Int {
        val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
        val startDate = sdf.parse(start)
        val endDate = sdf.parse(end)

        return if (startDate != null && endDate != null) {
            val diff = endDate.time - startDate.time
            val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
            if (days > 0) days else 1
        } else {
            1
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(toRadians(lat1)) * cos(
            toRadians(lat2)
        ) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

}
