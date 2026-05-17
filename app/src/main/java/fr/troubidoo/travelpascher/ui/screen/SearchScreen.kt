package fr.troubidoo.travelpascher.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiItinerary
import fr.troubidoo.travelpascher.viewmodel.UiPost
import fr.troubidoo.travelpascher.viewmodel.UiUser
import kotlinx.coroutines.delay
import fr.troubidoo.travelpascher.ui.screen.PostDetailDialog
import fr.troubidoo.travelpascher.ui.components.CommentDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: FeedViewModel,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onPostClick: (UiPost) -> Unit
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("Voyageurs", "Photos", "Parcours")

    var selectedFilter by remember { mutableStateOf("Tous") }
    val postFilters = listOf("Tous", "Plus aimés", "Plus récents")
    val itineraryFilters = listOf("Tous", "Restaurants", "Musées", "Parcs", "Hôtels")

    val userResults by viewModel.searchResultsUsers.collectAsState()
    val postResults by viewModel.searchResultsPosts.collectAsState()
    val itineraryResults by viewModel.searchResultsItineraries.collectAsState()
    val comments by viewModel.currentPostComments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedPostForDetail by remember { mutableStateOf<UiPost?>(null) }
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }

    // Logic for filtering
    val filteredPosts = remember(postResults, selectedFilter) {
        when (selectedFilter) {
            "Plus aimés" -> postResults.sortedByDescending { it.likedBy.size }
            "Plus récents" -> postResults.sortedByDescending { it.createdAt }
            else -> postResults
        }
    }

    val filteredItineraries = remember(itineraryResults, selectedFilter) {
        if (selectedFilter == "Tous") itineraryResults
        else itineraryResults.filter { it.activities.any { act -> act.category.contains(selectedFilter.dropLast(1), ignoreCase = true) } }
    }

    LaunchedEffect(searchQuery, selectedTabIndex) {
        if (searchQuery.length >= 2) {
            // delay(300) // Debounce
            when (selectedTabIndex) {
                0 -> viewModel.searchUsers(searchQuery)
                1 -> viewModel.searchPosts(searchQuery)
                2 -> viewModel.searchItinerariesInGlobal(searchQuery)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Rechercher...") },
                leadingIcon = { Icon(painterResource(R.drawable.outline_search_24), contentDescription = null, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            IconButton(onClick = { /* TODO: Show filter dialog */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtres")
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { 
                        selectedTabIndex = index 
                        selectedFilter = "Tous" // Reset filter on tab change
                    },
                    text = { Text(title) }
                )
            }
        }

        // Filter chips row
        if (selectedTabIndex > 0) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = if (selectedTabIndex == 1) postFilters else itineraryFilters
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    items(userResults) { user ->
                        UserSearchItem(user, onClick = { onUserClick(user.id) })
                    }
                }
                1 -> {
                    items(filteredPosts) { post ->
                        PostSearchItem(post, onClick = { selectedPostForDetail = post })
                    }
                }
                2 -> {
                    items(filteredItineraries) { itinerary ->
                        ItinerarySearchItem(itinerary)
                    }
                }
            }
        }
    }

    selectedPostForDetail?.let { post ->
        PostDetailDialog(
            post = post,
            isOwner = post.userId == currentUser?.uid,
            onDismiss = { selectedPostForDetail = null },
            onCommentClick = {
                selectedPostForDetail = null
                selectedPostIdForComments = post.id
                viewModel.listenToComments(post.id)
            }
        )
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
            onUpdateComment = { commentId, text -> viewModel.updateComment(postId, commentId, text) }
        )
    }
}

@Composable
fun UserSearchItem(user: UiUser, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(user.username, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(user.bio, maxLines = 1) },
        leadingContent = {
            AsyncImage(
                model = user.profileImageUrl.ifEmpty { R.drawable.outline_account_circle_24 },
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun PostSearchItem(post: UiPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = post.location, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Par ${post.username}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ItinerarySearchItem(itinerary: UiItinerary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = itinerary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = itinerary.destination, color = MaterialTheme.colorScheme.primary)
            Text(text = "${itinerary.activities.size} activités", style = MaterialTheme.typography.bodySmall)
        }
    }
}
