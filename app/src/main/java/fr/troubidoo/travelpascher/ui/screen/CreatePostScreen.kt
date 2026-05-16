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

@Composable
fun CreatePostScreen(viewModel: FeedViewModel, onPostSuccess: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    CreatePostContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onPostClick = { location, imageUri ->
            isLoading = true
            errorMessage = null
            viewModel.uploadPost(
                location = location,
                imageUri = imageUri,
                onSuccess = {
                    isLoading = false
                    onPostSuccess()
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    )
}

@Composable
fun CreatePostContent(
    isLoading: Boolean,
    errorMessage: String?,
    onPostClick: (String, Uri?) -> Unit
) {
    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

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
                .clickable(enabled = !isLoading) { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
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
            value = location,
            onValueChange = { location = it },
            label = { Text(stringResource(R.string.location_hint)) },
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
            onClick = { onPostClick(location, selectedImageUri) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && location.isNotBlank() && selectedImageUri != null,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreatePostScreenPreview() {
    TravelPasCherTheme {
        Surface {
            CreatePostContent(
                isLoading = false,
                errorMessage = null,
                onPostClick = { _, _ -> }
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
                onPostClick = { _, _ -> }
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
                errorMessage = stringResource(R.string.error_sending_post),
                onPostClick = { _, _ -> }
            )
        }
    }
}
