package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.components.CommentDialog
import fr.troubidoo.travelpascher.ui.components.Post
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiPost
import fr.troubidoo.travelpascher.viewmodel.UiStory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val comments by viewModel.currentPostComments.collectAsState()
    val selectedUserProfile by viewModel.selectedUserProfile.collectAsState()
    val selectedUserPosts by viewModel.selectedUserPosts.collectAsState()
    val selectedUserItineraries by viewModel.selectedUserItineraries.collectAsState()

    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }
    var selectedUserIdForProfile by remember { mutableStateOf<String?>(null) }
    var selectedPostForDetail by remember { mutableStateOf<UiPost?>(null) }

    val sheetState = rememberModalBottomSheetState()

    FeedScreenContent(
        stories = stories,
        posts = posts,
        isRefreshing = isRefreshing,
        currentUserId = currentUser?.uid,
        savedPosts = userData?.savedPosts ?: emptyList(),
        onRefresh = { viewModel.refresh() },
        onLikeClick = { postId -> viewModel.toggleLike(postId) },
        onBookmarkClick = { postId -> viewModel.toggleBookmark(postId) },
        onCommentClick = { postId ->
            selectedPostIdForComments = postId
            viewModel.listenToComments(postId)
        },
        onProfileClick = { userId ->
            selectedUserIdForProfile = userId
            viewModel.fetchUserProfile(userId)
        }
    )

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

    if (selectedUserIdForProfile != null && selectedUserProfile != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedUserIdForProfile = null
                viewModel.clearSelectedUserProfile()
            },
            sheetState = sheetState
        ) {
            ProfileContent(
                username = selectedUserProfile?.username ?: "",
                email = null,
                bio = selectedUserProfile?.bio ?: "",
                profileImageUrl = selectedUserProfile?.profileImageUrl ?: "",
                posts = selectedUserPosts,
                itineraries = selectedUserItineraries,
                isCurrentUser = selectedUserProfile?.id == currentUser?.uid,
                isFollowing = selectedUserProfile?.followers?.contains(currentUser?.uid ?: "")
                    ?: false,
                followersCount = selectedUserProfile?.followers?.size ?: 0,
                followingCount = selectedUserProfile?.following?.size ?: 0,
                onLogout = {},
                onPostClick = { post -> selectedPostForDetail = post },
                onSettingsClick = {},
                onFollowClick = { viewModel.toggleFollow(selectedUserProfile!!.id) }
            )
        }
    }

    selectedPostForDetail?.let { post ->
        PostDetailDialog(
            post = post,
            isOwner = post.userId == currentUser?.uid,
            onDismiss = { },
            onCommentClick = {
                post.id
                viewModel.listenToComments(post.id)
            }
        )
    }
}

@Composable
fun FeedScreenContent(
    stories: List<UiStory>,
    posts: List<UiPost>,
    isRefreshing: Boolean = false,
    currentUserId: String? = null,
    savedPosts: List<String> = emptyList(),
    onRefresh: () -> Unit = {},
    onLikeClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                StoriesSection(stories)
            }

            items(posts) { post ->
                Post(
                    username = post.username,
                    location = post.location,
                    description = post.description,
                    time = post.createdAt,
                    imageUrl = post.imageUrl,
                    authorProfileImageUrl = post.authorProfileImageUrl,
                    isLiked = currentUserId != null && post.likedBy.contains(currentUserId),
                    isBookmarked = savedPosts.contains(post.id),
                    likeCount = post.likedBy.size,
                    commentCount = post.commentCount,
                    latitude = post.latitude,
                    longitude = post.longitude,
                    onLikeClick = { onLikeClick(post.id) },
                    onSaveClick = { onBookmarkClick(post.id) },
                    onCommentClick = { onCommentClick(post.id) },
                    onProfileClick = { onProfileClick(post.userId) }
                )
            }
        }
    }
}

@Composable
fun StoriesSection(stories: List<UiStory>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            StoryItem(username = story.username)
        }
    }
}

@Composable
fun StoryItem(username: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(3.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_account_circle_24),
                contentDescription = stringResource(R.string.story),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = username,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    val sampleStories = listOf(
        UiStory("1", "Traveler1", ""),
        UiStory("2", "Alice", "")
    )

    val samplePosts = listOf(
        UiPost(
            "1",
            "1",
            "Traveler1",
            "",
            stringResource(R.string.location),
            "",
            "",
            System.currentTimeMillis()
        ),
        UiPost(
            "2",
            "2",
            "Alice",
            "",
            stringResource(R.string.location),
            "",
            "",
            System.currentTimeMillis() - 3600000
        )
    )

    FeedScreenContent(
        posts = samplePosts,
        stories = sampleStories,
        isRefreshing = false,
        currentUserId = null,
        onRefresh = {},
        onLikeClick = {},
        onCommentClick = {}
    )
}
