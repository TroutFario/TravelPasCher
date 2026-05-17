package fr.troubidoo.travelpascher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import fr.troubidoo.travelpascher.ui.screen.MainScreen
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        android.util.Log.d("TravelPasCher", "App Check Debug Provider installé.")

        // Initialisation de Google Places
        if (!Places.isInitialized()) {
            // Remplacez par votre clé API réelle
            Places.initializeWithNewPlacesApiEnabled(applicationContext, "AIzaSyADbS8kOjk5zfe5-ZbROmIf6CNn_13btbs")
        }

        enableEdgeToEdge()

        // Création du ViewModel
        val viewModel = FeedViewModel()

        setContent {
            TravelPasCherTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
