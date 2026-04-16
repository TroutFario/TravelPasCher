package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import fr.troubidoo.travelpascher.data.PostEntity
import fr.troubidoo.travelpascher.data.PostWithUser
import fr.troubidoo.travelpascher.data.StoryEntity
import fr.troubidoo.travelpascher.data.StoryWithUser
import fr.troubidoo.travelpascher.data.UserEntity
import fr.troubidoo.travelpascher.ui.components.Post
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

@Composable
fun FeedScreen(viewModel: FeedViewModel) {
    val posts = viewModel.posts.collectAsState(initial = emptyList()).value
    val stories = viewModel.stories.collectAsState(initial = emptyList()).value
    FeedScreenContent(stories = stories, posts = posts)
}

@Composable
fun FeedScreenContent(stories: List<StoryWithUser>, posts: List<PostWithUser>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Les stories deviennent le premier item du scroll
        item {
            StoriesSection(stories)
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // 2. Les posts suivent
        items(posts) { postWithUser ->
            Post(
                username = postWithUser.user.username,
                location = postWithUser.post.location,
                time = stringResource(R.string.time_ago, "2h"),
                imageRes = postWithUser.post.imageRes
            )
        }
    }
}

@Composable
fun StoriesSection(stories: List<StoryWithUser>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { storyWithUser ->
            StoryItem(username = storyWithUser.user.username)
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
    val dummyUser = UserEntity(1, "Traveler1", "", "", "", 0)

    val sampleStories = listOf(
        StoryWithUser(StoryEntity(0, 1, R.drawable.chevoul, 0), dummyUser),
        StoryWithUser(StoryEntity(1, 1, R.drawable.chevoul, 0), dummyUser),
    )

    val samplePosts = listOf(
        PostWithUser(PostEntity(0, 1, "Paris", R.drawable.chevoul, 0), dummyUser),
        PostWithUser(PostEntity(1, 1, "Lyon", R.drawable.chevoul, 0), dummyUser)
    )

    FeedScreenContent(posts = samplePosts, stories = sampleStories)
}