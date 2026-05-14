package fr.troubidoo.travelpascher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import fr.troubidoo.travelpascher.data.*
import fr.troubidoo.travelpascher.ui.screen.AuthScreen
import fr.troubidoo.travelpascher.ui.screen.FeedScreenContent
import fr.troubidoo.travelpascher.ui.screen.MainScreen
import fr.troubidoo.travelpascher.ui.screen.MainScreenContent
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiPost
import fr.troubidoo.travelpascher.viewmodel.UiStory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation de Firebase et App Check Debug
        // Ce token s'affichera dans le Logcat au démarrage de l'app (filtrez sur "AppCheckDebug")
        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        android.util.Log.d("TravelPasCher", "App Check Debug Provider installé.")

        enableEdgeToEdge()

        // Initialisation de la BDD locale (Room)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "travel-pas-cher-db"
        ).build()

        val postDao = db.postDao()
        val storyDao = db.storyDao()
        val userDao = db.userDao()

        // Création du ViewModel
        val viewModel = FeedViewModel(postDao, storyDao, userDao)

        setContent {
            TravelPasCherTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainActivityPreview() {
    val sampleStories = listOf(
        UiStory("1", "Traveler1", ""),
        UiStory("2", "Alice", "")
    )

    val samplePosts = listOf(
        UiPost("1", "Traveler1", "Paris", "", System.currentTimeMillis()),
        UiPost("2", "Alice", "Lyon", "", System.currentTimeMillis() - 3600000)
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    TravelPasCherTheme {
        MainScreenContent(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        ) {
            when (selectedTab) {
                0 -> FeedScreenContent(posts = samplePosts, stories = sampleStories)
                3 -> AuthScreen()
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Écran $selectedTab")
                }
            }
        }
    }
}
