package fr.troubidoo.travelpascher.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val location: String = "",
    val selectedImageUri: Uri? = null
)

@Composable
private fun CreatePostContent(
    uiState: CreatePostUiState,
    onLocationChange: (String) -> Unit,
    onImageClick: () -> Unit,
    onPostClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.share_travel_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Zone de sélection d'image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable(enabled = !uiState.isLoading) { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.selectedImageUri != null) {
                AsyncImage(
                    model = uiState.selectedImageUri,
                    contentDescription = stringResource(R.string.selected_image_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.add_photo_label), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.location,
            onValueChange = onLocationChange,
            label = { Text(stringResource(R.string.location_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isLoading
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onPostClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = (!uiState.isLoading && uiState.location.isNotBlank() && uiState.selectedImageUri != null),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.publish_button))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CreatePostScreen(viewModel: FeedViewModel, onPostSuccess: () -> Unit) {
    var uiState by remember { mutableStateOf(CreatePostUiState()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uiState = uiState.copy(selectedImageUri = uri)
    }

    CreatePostContent(
        uiState = uiState,
        onLocationChange = { uiState = uiState.copy(location = it) },
        onImageClick = { launcher.launch("image/*") },
        onPostClick = {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModel.uploadPost(
                location = uiState.location,
                imageUri = uiState.selectedImageUri,
                onSuccess = {
                    uiState = uiState.copy(isLoading = false)
                    onPostSuccess()
                },
                onError = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error)
                }
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreatePostScreenPreview() {
    TravelPasCherTheme {
        Surface {
            CreatePostContent(
                uiState = CreatePostUiState(),
                onLocationChange = {},
                onImageClick = {},
                onPostClick = {}
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
                uiState = CreatePostUiState(isLoading = true, location = "Paris"),
                onLocationChange = {},
                onImageClick = {},
                onPostClick = {}
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
                uiState = CreatePostUiState(
                    errorMessage = stringResource(R.string.error_sending_post),
                    location = "Lyon"
                ),
                onLocationChange = {},
                onImageClick = {},
                onPostClick = {}
            )
        }
    }
}
