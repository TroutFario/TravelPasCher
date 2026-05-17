package fr.troubidoo.travelpascher.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.components.CommentDialog
import fr.troubidoo.travelpascher.ui.components.Post
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsScreen(viewModel: FeedViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val posts by viewModel.posts.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val comments by viewModel.currentPostComments.collectAsState()

    val savedPosts = remember(posts, userData?.savedPosts) {
        posts.filter { userData?.savedPosts?.contains(it.id) == true }
    }

    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
            Text(
                text = "Mes Enregistrements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        if (savedPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aucun post enregistré pour le moment.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(savedPosts) { post ->
                    Post(
                        username = post.username,
                        location = post.location,
                        time = post.createdAt,
                        imageUrl = post.imageUrl,
                        authorProfileImageUrl = post.authorProfileImageUrl,
                        isLiked = currentUser != null && post.likedBy.contains(currentUser!!.uid),
                        isBookmarked = true,
                        likeCount = post.likedBy.size,
                        commentCount = post.commentCount,
                        latitude = post.latitude,
                        longitude = post.longitude,
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onSaveClick = { viewModel.toggleBookmark(post.id) },
                        onCommentClick = {
                            selectedPostIdForComments = post.id
                            viewModel.listenToComments(post.id)
                        }
                    )
                }
            }
        }
    }

    selectedPostIdForComments?.let { postId ->
        CommentDialog(
            comments = comments,
            currentUserId = currentUser?.uid,
            onDismiss = {
                selectedPostIdForComments = null
                viewModel.stopListeningToComments()
            },
            onSendComment = { text -> viewModel.addComment(postId, text) },
            onDeleteComment = { commentId -> viewModel.deleteComment(postId, commentId) },
            onUpdateComment = { commentId, text ->
                viewModel.updateComment(
                    postId,
                    commentId,
                    text
                )
            }
        )
    }
}
