package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiPost
import fr.troubidoo.travelpascher.viewmodel.UiStory

@Composable
fun MainScreen(viewModel: FeedViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.currentUser.collectAsState()

    MainScreenContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        content = {
            when (selectedTab) {
                0 -> FeedScreen(viewModel = viewModel)
                1 -> CreatePostScreen(
                    viewModel = viewModel,
                    onPostSuccess = { selectedTab = 0 }
                )
                3 -> {
                    if (currentUser != null) {
                        ProfileScreen(viewModel = viewModel)
                    } else {
                        AuthScreen(viewModel = viewModel)
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Écran en cours de développement")
                    }
                }
            }
        }
    )
}

@Composable
fun ProfileScreen(viewModel: FeedViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Connecté en tant que : ${currentUser?.email ?: "Inconnu"}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.logout() }) {
            Text("Se déconnecter")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(elevation = 4.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(2.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_globe_24),
                                    contentDescription = stringResource(R.string.my_profile),
                                    modifier = Modifier.fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                    IconButton(onClick = { /* TODO */ }) {
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
        },
        bottomBar = {
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
                        selected = selectedTab == 0,
                        onClick = { onTabSelected(0) },
                        icon = { Icon(painterResource(id = R.drawable.outline_home_24), contentDescription = stringResource(R.string.home_button), modifier = Modifier.size(24.dp)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        icon = { Icon(painterResource(id = R.drawable.outline_add_photo_alternate_24), contentDescription = stringResource(R.string.add_image_button), modifier = Modifier.size(24.dp)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { onTabSelected(2) },
                        icon = { Icon(painterResource(id = R.drawable.outline_globe_24), contentDescription = stringResource(R.string.globe), modifier = Modifier.size(24.dp)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { onTabSelected(3) },
                        icon = { Icon(painterResource(id = R.drawable.outline_account_circle_24), contentDescription = stringResource(R.string.profile), modifier = Modifier.size(24.dp)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    val sampleStories = listOf(
        UiStory(1, "Traveler1", ""),
        UiStory(2, "Alice", "")
    )

    val samplePosts = listOf(
        UiPost(1, "Traveler1", "Paris", "", System.currentTimeMillis()),
        UiPost(2, "Alice", "Lyon", "", System.currentTimeMillis() - 3600000)
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    TravelPasCherTheme {
        MainScreenContent(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        ) {
            when (selectedTab) {
                0 -> FeedScreenContent(posts = samplePosts, stories = sampleStories)
                1 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Écran de création de post")
                }
                3 -> AuthScreen()
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Écran $selectedTab")
                }
            }
        }
    }
}
