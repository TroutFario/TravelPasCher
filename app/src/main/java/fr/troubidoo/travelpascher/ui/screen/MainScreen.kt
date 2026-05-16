package fr.troubidoo.travelpascher.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.screen.auth.AuthScreen
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.*

@Composable
fun MainScreen(viewModel: FeedViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsState()
    val userData by viewModel.userData.collectAsState()

    MainScreenContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        userData = userData,
        content = {
            if (showSettings) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { showSettings = false }
                )
            } else {
                when (selectedTab) {
                    0 -> FeedScreen(viewModel = viewModel)
                    1 -> CreatePostScreen(
                        viewModel = viewModel,
                        onPostSuccess = { selectedTab = 0 }
                    )
                    3 -> {
                        if (currentUser != null) {
                            ProfileScreen(
                                viewModel = viewModel,
                                onSettingsClick = { showSettings = true }
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

@Composable
fun ProfileScreen(viewModel: FeedViewModel, onSettingsClick: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    var selectedPost by remember { mutableStateOf<UiPost?>(null) }

    val userPosts = remember(allPosts, currentUser) {
        allPosts.filter { it.userId == currentUser?.uid }
    }

    ProfileContent(
        username = userData?.username ?: currentUser?.displayName ?: currentUser?.email?.split("@")?.firstOrNull() ?: stringResource(R.string.default_username),
        email = currentUser?.email,
        bio = userData?.bio ?: stringResource(R.string.profile_bio),
        profileImageUrl = userData?.profileImageUrl ?: "",
        posts = userPosts,
        onLogout = { viewModel.logout() },
        onPostClick = { selectedPost = it },
        onSettingsClick = onSettingsClick
    )

    if (selectedPost != null) {
        PostDetailDialog(
            post = selectedPost!!,
            onDismiss = { selectedPost = null },
            onUpdate = { newLocation ->
                viewModel.updatePost(selectedPost!!.id, newLocation)
                selectedPost = null
            },
            onDelete = {
                viewModel.deletePost(selectedPost!!.id, selectedPost!!.imageUrl)
                selectedPost = null
            }
        )
    }
}

@Composable
fun PostDetailDialog(
    post: UiPost,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(post.location) }

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
                    .padding(16.dp),
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdate(location) },
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_action), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_action), tint = MaterialTheme.colorScheme.error)
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
    onLogout: () -> Unit,
    onPostClick: (UiPost) -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHeader(username, email, bio, profileImageUrl, posts.size, onLogout, onSettingsClick)
        
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(posts) { post ->
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
                            modifier = Modifier.align(Alignment.Center).size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
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
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit
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
                ProfileStat(number = postCount.toString(), label = stringResource(R.string.stats_posts))
                ProfileStat(number = "124", label = stringResource(R.string.stats_followers))
                ProfileStat(number = "89", label = stringResource(R.string.stats_following))
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
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userData: UiUser? = null,
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
            selectedTab = 0,
            onTabSelected = {},
            userData = null
        ) {
            FeedScreenContent(posts = samplePosts, stories = sampleStories)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Création de Post")
@Composable
fun MainScreenCreatePostPreview() {
    TravelPasCherTheme {
        MainScreenContent(
            selectedTab = 1,
            onTabSelected = {},
            userData = null
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.create_post_screen_preview_text))
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: FeedViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val userData by viewModel.userData.collectAsState()
    var firstName by remember(userData) { mutableStateOf(userData?.firstName ?: "") }
    var lastName by remember(userData) { mutableStateOf(userData?.lastName ?: "") }
    var bio by remember(userData) { mutableStateOf(userData?.bio ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Zone de sélection de photo de profil
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(enabled = !isLoading) { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!userData?.profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = userData?.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text(stringResource(R.string.firstname_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text(stringResource(R.string.lastname_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text(stringResource(R.string.bio_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !isLoading
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val isSuccess = message == context.getString(R.string.profile_updated_success)
            Text(
                text = message!!,
                color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.updateUserProfile(
                    firstName = firstName,
                    lastName = lastName,
                    bio = bio,
                    newProfileImageUri = selectedImageUri,
                    onSuccess = {
                        isLoading = false
                        message = context.getString(R.string.profile_updated_success)
                        selectedImageUri = null // Reset selection after success
                    },
                    onError = {
                        isLoading = false
                        message = it
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.save_profile_button))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Profil")
@Composable
fun MainScreenProfilePreview() {
    val samplePosts = listOf(
        UiPost("1", "1", "User", "", "Paris", "", System.currentTimeMillis()),
        UiPost("2", "1", "User", "", "London", "", System.currentTimeMillis()),
        UiPost("3", "1", "User", "", "Tokyo", "", System.currentTimeMillis())
    )
    TravelPasCherTheme {
        MainScreenContent(
            selectedTab = 3,
            onTabSelected = {},
            userData = null
        ) {
            ProfileContent(
                username = "Traveler_Expert",
                email = "test@example.com",
                bio = "Aventureux!",
                profileImageUrl = "",
                posts = samplePosts,
                onLogout = {},
                onPostClick = {},
                onSettingsClick = {}
            )
        }
    }
}
