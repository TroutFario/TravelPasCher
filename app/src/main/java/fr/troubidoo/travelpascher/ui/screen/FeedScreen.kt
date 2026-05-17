package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import fr.troubidoo.travelpascher.ui.components.Post
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiPost
import fr.troubidoo.travelpascher.viewmodel.UiStory

@Composable
fun FeedScreen(viewModel: FeedViewModel) {
    // On collecte les StateFlow de Firebase
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    FeedScreenContent(
        stories = stories,
        posts = posts,
        isRefreshing = isRefreshing,
        currentUserId = currentUser?.uid,
        onRefresh = { viewModel.refresh() },
        onLikeClick = { postId -> viewModel.toggleLike(postId) }
    )
}

@Composable
fun FeedScreenContent(
    stories: List<UiStory>,
    posts: List<UiPost>,
    isRefreshing: Boolean = false,
    currentUserId: String? = null,
    onRefresh: () -> Unit = {},
    onLikeClick: (String) -> Unit = {}
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
                    time = post.createdAt,
                    imageUrl = post.imageUrl,
                    authorProfileImageUrl = post.authorProfileImageUrl,
                    isLiked = currentUserId != null && post.likedBy.contains(currentUserId),
                    likeCount = post.likedBy.size,
                    onLikeClick = { onLikeClick(post.id) }
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
        UiPost("1", "1", "Traveler1", "", stringResource(R.string.location), "", System.currentTimeMillis()),
        UiPost("2", "2", "Alice", "", stringResource(R.string.location), "", System.currentTimeMillis() - 3600000)
    )

    FeedScreenContent(
        posts = samplePosts,
        stories = sampleStories,
        isRefreshing = false,
        currentUserId = null,
        onRefresh = {},
        onLikeClick = {}
    )
}
