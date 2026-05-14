package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

@Composable
fun CreatePostScreen(viewModel: FeedViewModel, onPostSuccess: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    CreatePostContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onPostClick = { location ->
            viewModel.uploadPost(
                location = location,
                onSuccess = {
                    onPostSuccess()
                },
                onError = {
                }
            )
        }
    )
}

@Composable
fun CreatePostContent(
    isLoading: Boolean,
    errorMessage: String?,
    onPostClick: (String) -> Unit
) {
    var location by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Partager un voyage",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Lieu (ex: Paris, France)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onPostClick(location) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && location.isNotBlank(),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Publier")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreatePostScreenPreview() {
    TravelPasCherTheme {
        Surface {
            CreatePostContent(
                isLoading = false,
                errorMessage = null,
                onPostClick = { _ -> }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading State")
@Composable
fun CreatePostScreenLoadingPreview() {
    TravelPasCherTheme {
        Surface {
            CreatePostContent(
                isLoading = true,
                errorMessage = null,
                onPostClick = { _ -> }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error State")
@Composable
fun CreatePostScreenErrorPreview() {
    TravelPasCherTheme {
        Surface {
            CreatePostContent(
                isLoading = false,
                errorMessage = "Une erreur est survenue lors de l'envoi",
                onPostClick = { _ -> }
            )
        }
    }
}
