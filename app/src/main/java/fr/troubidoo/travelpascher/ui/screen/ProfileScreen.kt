package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.components.CommentDialog
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiActivity
import fr.troubidoo.travelpascher.viewmodel.UiItinerary
import fr.troubidoo.travelpascher.viewmodel.UiPost
import androidx.compose.foundation.lazy.grid.items as itemsGrid
import androidx.compose.foundation.lazy.items as itemsList

data class ProfileUiState(
    val selectedPost: UiPost? = null
)

@Composable
fun ProfileScreen(viewModel: FeedViewModel, onSettingsClick: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val allItineraries by viewModel.itineraries.collectAsState()
    val comments by viewModel.currentPostComments.collectAsState()
    var profileState by remember { mutableStateOf(ProfileUiState()) }
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }

    val userPosts = remember(allPosts, currentUser) {
        allPosts.filter { it.userId == currentUser?.uid }
    }

    val userItineraries = remember(allItineraries, currentUser) {
        allItineraries.filter { it.userId == currentUser?.uid }
    }

    val savedPosts = remember(allPosts, userData?.savedPosts) {
        allPosts.filter { userData?.savedPosts?.contains(it.id) == true }
    }

    ProfileContent(
        username = userData?.username ?: currentUser?.displayName ?: currentUser?.email?.split("@")
            ?.firstOrNull() ?: stringResource(R.string.default_username),
        email = currentUser?.email,
        bio = userData?.bio ?: stringResource(R.string.profile_bio),
        profileImageUrl = userData?.profileImageUrl ?: "",
        posts = userPosts,
        itineraries = userItineraries,
        savedPosts = savedPosts,
        isCurrentUser = true,
        followersCount = userData?.followers?.size ?: 0,
        followingCount = userData?.following?.size ?: 0,
        onLogout = { viewModel.logout() },
        onPostClick = { profileState = profileState.copy(selectedPost = it) },
        onSettingsClick = onSettingsClick,
        onDeleteItinerary = { viewModel.deleteItinerary(it) },
        onAddActivityToItinerary = { },
        onDeleteActivityFromItinerary = { id, act ->
            viewModel.deleteActivityFromItinerary(
                id,
                act
            )
        }
    )

    profileState.selectedPost?.let { post ->
        PostDetailDialog(
            post = post,
            isOwner = true,
            onDismiss = { profileState = profileState.copy(selectedPost = null) },
            onUpdate = { newLocation, newDesc ->
                viewModel.updatePost(post.id, newLocation, newDesc)
                profileState = profileState.copy(selectedPost = null)
            },
            onDelete = {
                viewModel.deletePost(post.id, post.imageUrl)
                profileState = profileState.copy(selectedPost = null)
            },
            onCommentClick = {
                profileState = profileState.copy(selectedPost = null)
                selectedPostIdForComments = post.id
                viewModel.listenToComments(post.id)
            }
        )
    }

    selectedPostIdForComments?.let { postId: String ->
        CommentDialog(
            comments = comments,
            currentUserId = currentUser?.uid,
            onDismiss = {
                selectedPostIdForComments = null
                viewModel.stopListeningToComments()
            },
            onSendComment = { text: String -> viewModel.addComment(postId, text) },
            onDeleteComment = { commentId: String -> viewModel.deleteComment(postId, commentId) },
            onUpdateComment = { commentId: String, text: String ->
                viewModel.updateComment(
                    postId,
                    commentId,
                    text
                )
            }
        )
    }
}

@Composable
fun PostDetailDialog(
    post: UiPost,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String) -> Unit = { _, _ -> },
    onDelete: () -> Unit = {},
    onCommentClick: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(post.location) }
    var description by remember { mutableStateOf(post.description) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(stringResource(R.string.label_location)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdate(location, description) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.save_button))
                        }
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel_button))
                        }
                    }
                } else {
                    Text(
                        text = post.location,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (post.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (isOwner) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_action),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = onCommentClick) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = stringResource(R.string.comment),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isOwner) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_action),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    username: String,
    email: String?,
    bio: String,
    profileImageUrl: String,
    posts: List<UiPost>,
    itineraries: List<UiItinerary>,
    savedPosts: List<UiPost> = emptyList(),
    isCurrentUser: Boolean = true,
    isFollowing: Boolean = false,
    followersCount: Int = 0,
    followingCount: Int = 0,
    onLogout: () -> Unit = {},
    onPostClick: (UiPost) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    onDeleteItinerary: (String) -> Unit = {},
    onAddActivityToItinerary: (String) -> Unit = {},
    onDeleteActivityFromItinerary: (String, UiActivity) -> Unit = { _, _ -> }
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = if (isCurrentUser) listOf("Posts", "Parcours", "Enregistrés") else listOf(
        "Posts",
        "Parcours"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHeader(
            username, email, bio, profileImageUrl, posts.size,
            isCurrentUser, isFollowing, followersCount, followingCount,
            onLogout, onSettingsClick, onFollowClick
        )

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.GridOn
                                1 -> Icons.Default.Map
                                else -> Icons.Default.Bookmark
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                ProfilePostsGrid(posts, onPostClick)
            }

            1 -> {
                ProfileItinerariesList(
                    itineraries,
                    isCurrentUser,
                    onDeleteItinerary,
                    onAddActivityToItinerary,
                    onDeleteActivityFromItinerary
                )
            }

            2 -> {
                ProfilePostsGrid(savedPosts, onPostClick)
            }
        }
    }
}

@Composable
fun ProfilePostsGrid(posts: List<UiPost>, onPostClick: (UiPost) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        itemsGrid(posts) { post ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPostClick(post) }
            ) {
                if (post.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_add_photo_alternate_24),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileItinerariesList(
    itineraries: List<UiItinerary>,
    isCurrentUser: Boolean,
    onDeleteItinerary: (String) -> Unit,
    onAddActivityToItinerary: (String) -> Unit,
    onDeleteActivityFromItinerary: (String, UiActivity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsList(itineraries) { itinerary ->
            ItineraryCard(
                itinerary = itinerary,
                isOwner = isCurrentUser,
                onDelete = { onDeleteItinerary(itinerary.id) },
                onAddActivity = { onAddActivityToItinerary(itinerary.id) },
                onDeleteActivity = { activity ->
                    onDeleteActivityFromItinerary(
                        itinerary.id,
                        activity
                    )
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(
    username: String,
    email: String?,
    bio: String,
    profileImageUrl: String,
    postCount: Int,
    isCurrentUser: Boolean = true,
    isFollowing: Boolean = false,
    followersCount: Int = 0,
    followingCount: Int = 0,
    onLogout: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFollowClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_account_circle_24),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    number = postCount.toString(),
                    label = stringResource(R.string.stats_posts)
                )
                ProfileStat(
                    number = followersCount.toString(),
                    label = stringResource(R.string.stats_followers)
                )
                ProfileStat(
                    number = followingCount.toString(),
                    label = stringResource(R.string.stats_following)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = bio,
            style = MaterialTheme.typography.bodyMedium
        )
        if (email != null) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isCurrentUser) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(R.string.edit_profile_button))
                }
                OutlinedButton(
                    onClick = onLogout,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(R.string.logout_button))
                }
            }
        } else {
            Button(
                onClick = onFollowClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = if (isFollowing) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                } else {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                }
            ) {
                Text(if (isFollowing) stringResource(R.string.unfollow_button) else stringResource(R.string.follow_button))
            }
        }
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Profil")
@Composable
fun ProfileScreenPreview() {
    val samplePosts = listOf(
        UiPost("1", "1", "User", "", "Paris", "", "", System.currentTimeMillis()),
        UiPost("2", "1", "User", "", "London", "", "", System.currentTimeMillis()),
        UiPost("3", "1", "User", "", "Tokyo", "", "", System.currentTimeMillis())
    )
    TravelPasCherTheme {
        ProfileContent(
            username = "Traveler_Expert",
            email = "test@example.com",
            bio = "Aventureux!",
            profileImageUrl = "",
            posts = samplePosts,
            itineraries = emptyList(),
            onLogout = {},
            onPostClick = {},
            onSettingsClick = {}
        )
    }
}
