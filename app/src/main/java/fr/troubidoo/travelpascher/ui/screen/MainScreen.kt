package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.screen.auth.AuthScreen
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.*

// --- États UI ---

data class MainUiState(
    val currentTab: Int = 0,
    val isSettingsVisible: Boolean = false
)

// --- Composants principaux ---

@Composable
fun MainScreen(viewModel: FeedViewModel) {
    var uiState by remember { mutableStateOf(MainUiState()) }
    val currentUser by viewModel.currentUser.collectAsState()
    val userData by viewModel.userData.collectAsState()

    MainScreenContent(
        currentTab = uiState.currentTab,
        onTabSelected = { uiState = uiState.copy(currentTab = it) },
        userData = userData,
        showBars = !uiState.isSettingsVisible,
        content = {
            if (uiState.isSettingsVisible) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { uiState = uiState.copy(isSettingsVisible = false) }
                )
            } else {
                when (uiState.currentTab) {
                    0 -> FeedScreen(viewModel = viewModel)
                    1 -> ExploreScreen()
                    2 -> CreatePostScreen(
                        viewModel = viewModel,
                        onPostSuccess = { uiState = uiState.copy(currentTab = 0) }
                    )
                    3 -> ItineraryScreen(viewModel = viewModel)
                    4 -> {
                        if (currentUser != null) {
                            ProfileScreen(
                                viewModel = viewModel,
                                onSettingsClick = { uiState = uiState.copy(isSettingsVisible = true) }
                            )
                        } else {
                            AuthScreen(viewModel = viewModel)
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.screen_under_development))
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    userData: UiUser? = null,
    showBars: Boolean = true,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBars) {
                TopAppBar(
                    modifier = Modifier.shadow(elevation = 4.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { onTabSelected(4) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        .padding(2.dp)
                                ) {
                                    if (userData?.profileImageUrl?.isNotEmpty() == true) {
                                        AsyncImage(
                                            model = userData.profileImageUrl,
                                            contentDescription = stringResource(R.string.my_profile),
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_globe_24),
                                            contentDescription = stringResource(R.string.my_profile),
                                            modifier = Modifier.fillMaxSize(),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onTabSelected(2) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_send_24),
                                contentDescription = stringResource(R.string.add_image_button),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                )
            }
        },
        bottomBar = {
            if (showBars) {
                BottomAppBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(64.dp),
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    contentPadding = PaddingValues(0.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { onTabSelected(0) },
                            icon = { Icon(painterResource(id = R.drawable.outline_home_24), contentDescription = stringResource(R.string.home_button), modifier = Modifier.size(24.dp)) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { onTabSelected(1) },
                            icon = { Icon(painterResource(id = R.drawable.outline_globe_24), contentDescription = stringResource(R.string.globe), modifier = Modifier.size(24.dp)) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { onTabSelected(2) },
                            icon = { Icon(painterResource(id = R.drawable.outline_add_photo_alternate_24), contentDescription = stringResource(R.string.add_image_button), modifier = Modifier.size(24.dp)) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { onTabSelected(3) },
                            icon = { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.itineraries), modifier = Modifier.size(24.dp)) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { onTabSelected(4) },
                            icon = { Icon(painterResource(id = R.drawable.outline_account_circle_24), contentDescription = stringResource(R.string.profile), modifier = Modifier.size(24.dp)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Flux d'actualité")
@Composable
fun MainScreenFeedPreview() {
    val sampleStories = listOf(
        UiStory("1", "Traveler1", ""),
        UiStory("2", "Alice", "")
    )
    val samplePosts = listOf(
        UiPost("1", "1", "Traveler1", "", "Paris", "", System.currentTimeMillis()),
        UiPost("2", "2", "Alice", "", "Lyon", "", System.currentTimeMillis() - 3600000)
    )

    TravelPasCherTheme {
        MainScreenContent(
            currentTab = 0,
            onTabSelected = {},
            userData = null
        ) {
            FeedScreenContent(
                posts = samplePosts,
                stories = sampleStories,
                isRefreshing = false,
                currentUserId = null,
                onRefresh = {},
                onLikeClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Création de Post")
@Composable
fun MainScreenCreatePostPreview() {
    TravelPasCherTheme {
        MainScreenContent(
            currentTab = 2,
            onTabSelected = {},
            userData = null
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.create_post_screen_preview_text))
            }
        }
    }
}
