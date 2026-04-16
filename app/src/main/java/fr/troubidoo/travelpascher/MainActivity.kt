package fr.troubidoo.travelpascher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import fr.troubidoo.travelpascher.data.AppDatabase
import fr.troubidoo.travelpascher.ui.screen.MainScreen
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
