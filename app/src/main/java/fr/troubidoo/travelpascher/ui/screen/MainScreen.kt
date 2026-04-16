package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.data.*
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

@Composable
fun MainScreen(viewModel: FeedViewModel) {
    MainScreenContent(
        content = {
            FeedScreen(viewModel = viewModel)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(content: @Composable () -> Unit) {
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
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.height(60.dp),
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* TODO */ },
                    icon = { Icon(painterResource(id = R.drawable.outline_home_24), contentDescription = stringResource(R.string.home_button), modifier = Modifier.size(24.dp)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(painterResource(id = R.drawable.outline_add_photo_alternate_24), contentDescription = stringResource(R.string.add_image_button), modifier = Modifier.size(24.dp)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(painterResource(id = R.drawable.outline_globe_24), contentDescription = stringResource(R.string.globe), modifier = Modifier.size(24.dp)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(painterResource(id = R.drawable.outline_account_circle_24), contentDescription = stringResource(R.string.profile), modifier = Modifier.size(24.dp)) }
                )
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
    val dummyUser = UserEntity(1, "Traveler1", "", "", "", 0)
    
    val sampleStories = listOf(
        StoryWithUser(StoryEntity(0, 1, R.drawable.chevoul, 0), dummyUser),
        StoryWithUser(StoryEntity(1, 1, R.drawable.chevoul, 0), dummyUser),
    )

    val samplePosts = listOf(
        PostWithUser(PostEntity(0, 1, "Paris", R.drawable.chevoul, 0), dummyUser),
        PostWithUser(PostEntity(1, 1, "Lyon", R.drawable.chevoul, 0), dummyUser)
    )

    TravelPasCherTheme {
        MainScreenContent {
            FeedScreenContent(posts = samplePosts, stories = sampleStories)
        }
    }
}
