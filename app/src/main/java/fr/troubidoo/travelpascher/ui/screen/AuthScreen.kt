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
            AuthStep.LOGIN -> LoginContent(onBack = { currentStep = AuthStep.CHOICE })
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

@Composable
fun LoginContent(onBack: () -> Unit) {
    Text(text = "Connexion", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(24.dp))
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
        Text("Se connecter")
    }
    TextButton(onClick = onBack) {
        Text("Retour")
    }
}

@Composable
fun SignupContent(
    viewModel: FeedViewModel? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit
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
            if (viewModel != null && username.isNotBlank() && email.isNotBlank()) {
                isLoading = true
                // On génère un ID temporaire pour le test (ou on laisse Firebase le faire)
                // Ici, on utilise un hashCode pour l'exemple.
                val id = (username + email).hashCode()
                viewModel.registerUser(
                    id = id,
                    username = username,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
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
            Text("S'inscrire")
        }
    }
    TextButton(onClick = onBack) {
        Text("Retour")
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    TravelPasCherTheme {
        AuthScreen()
    }
}
