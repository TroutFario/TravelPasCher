package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

enum class AuthStep {
    CHOICE, LOGIN, SIGNUP
}

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

@Composable
fun ChoiceContent(onLoginClick: () -> Unit, onSignupClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Bienvenue sur TravelPasCher",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "L'aventure commence ici",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Se connecter")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onSignupClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Créer un compte")
        }
    }
}

@Composable
fun LoginContent(
    viewModel: FeedViewModel? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Connexion", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val trimmedEmail = email.trim()
                if (viewModel != null && trimmedEmail.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    viewModel.loginUser(
                        email = trimmedEmail,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onSuccess()
                        },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Se connecter")
            }
        }
        TextButton(onClick = onBack, enabled = !isLoading) {
            Text("Retour")
        }
    }
}

@Composable
fun SignupContent(
    viewModel: FeedViewModel? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Inscription", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") } // Gardé pour l'UI, pas encore utilisé dans Firebase
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }

        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Prénom") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Nom") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                val trimmedEmail = email.trim()
                if (viewModel != null && username.isNotBlank() && trimmedEmail.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    viewModel.registerUser(
                        username = username.trim(),
                        email = trimmedEmail,
                        password = password,
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        onSuccess = {
                            isLoading = false
                            onSuccess()
                        },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                } else if (password.isBlank()) {
                    errorMessage = "Le mot de passe ne peut pas être vide"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("S'inscrire")
            }
        }
        TextButton(onClick = onBack) {
            Text("Retour")
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

@Preview(showBackground = true)
@Composable
fun ChoiceContentPreview() {
    TravelPasCherTheme {
        ChoiceContent(onLoginClick = {}, onSignupClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    TravelPasCherTheme {
        LoginContent(onBack = {}, onSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SignupContentPreview() {
    TravelPasCherTheme {
        SignupContent(onBack = {}, onSuccess = {})
    }
}
