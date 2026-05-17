package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import fr.troubidoo.travelpascher.ui.components.Post
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: FeedViewModel) {
    val posts by viewModel.posts.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val followingList = userData?.following ?: emptyList()
    val explorePosts = remember(posts, followingList, currentUser) {
        posts.filter { it.userId in followingList || it.userId == currentUser?.uid }
            .filter { it.latitude != null && it.longitude != null }
    }

    var selectedPost by remember { mutableStateOf<UiPost?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 3f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            explorePosts.forEach { post ->
                val position = LatLng(post.latitude!!, post.longitude!!)
                Marker(
                    state = rememberUpdatedMarkerState(position = position),
                    title = post.location,
                    snippet = "Par ${post.username}",
                    onClick = {
                        selectedPost = post
                        isSheetOpen = true
                        false
                    }
                )
            }
        }

        if (explorePosts.isEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "Suivez des voyageurs pour voir leurs étapes !",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (isSheetOpen && selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = {
                isSheetOpen = false
                selectedPost = null
            },
            sheetState = sheetState
        ) {
            Box(modifier = Modifier.padding(bottom = 32.dp)) {
                Post(
                    username = selectedPost!!.username,
                    location = selectedPost!!.location,
                    time = selectedPost!!.createdAt,
                    imageUrl = selectedPost!!.imageUrl,
                    authorProfileImageUrl = selectedPost!!.authorProfileImageUrl,
                    isLiked = currentUser != null && selectedPost!!.likedBy.contains(currentUser!!.uid),
                    likeCount = selectedPost!!.likedBy.size,
                    latitude = selectedPost!!.latitude,
                    longitude = selectedPost!!.longitude,
                    onLikeClick = { viewModel.toggleLike(selectedPost!!.id) }
                )
            }
        }
    }
}
