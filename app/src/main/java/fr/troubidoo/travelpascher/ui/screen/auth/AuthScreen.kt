package fr.troubidoo.travelpascher.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

@Composable
fun AuthScreen(viewModel: FeedViewModel? = null) {
    var currentStep by remember { mutableStateOf(AuthStep.CHOICE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            AuthStep.CHOICE -> ChoiceContent(
                onLoginClick = { currentStep = AuthStep.LOGIN },
                onSignupClick = { currentStep = AuthStep.SIGNUP }
            )
            AuthStep.LOGIN -> LoginContent(
                viewModel = viewModel,
                onBack = { currentStep = AuthStep.CHOICE },
                onSuccess = { /* La redirection est gérée par MainScreen via le StateFlow */ }
            )
            AuthStep.SIGNUP -> SignupContent(
                viewModel = viewModel,
                onBack = { currentStep = AuthStep.CHOICE },
                onSuccess = { currentStep = AuthStep.CHOICE }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    TravelPasCherTheme {
        AuthScreen()
    }
}
