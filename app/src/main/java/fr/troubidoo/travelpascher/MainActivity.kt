package fr.troubidoo.travelpascher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import fr.troubidoo.travelpascher.data.*
import fr.troubidoo.travelpascher.ui.screen.FeedScreenContent
import fr.troubidoo.travelpascher.ui.screen.MainScreen
import fr.troubidoo.travelpascher.ui.screen.MainScreenContent
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration(true)
            .build()

        val postDao = db.postDao()
        val storyDao = db.storyDao()
        val userDao = db.userDao()

        setContent {
            TravelPasCherTheme {
                val viewModel = FeedViewModel(postDao, storyDao, userDao)
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainActivityPreview() {
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
