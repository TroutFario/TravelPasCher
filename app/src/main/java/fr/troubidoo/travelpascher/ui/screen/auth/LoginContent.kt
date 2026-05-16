package fr.troubidoo.travelpascher.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

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
        Text(text = stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
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
                Text(stringResource(R.string.login_button))
            }
        }
        TextButton(onClick = onBack, enabled = !isLoading) {
            Text(stringResource(R.string.back_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    TravelPasCherTheme {
        LoginContent(onBack = {}, onSuccess = {})
    }
}
