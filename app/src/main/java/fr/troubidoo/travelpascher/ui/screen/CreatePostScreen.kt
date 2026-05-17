package fr.troubidoo.travelpascher.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiActivity
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val locationName: String = "",
    val description: String = "",
    val selectedImageUri: Uri? = null,
    val selectedLocation: LatLng? = null
)

@Composable
private fun CreatePostContent(
    uiState: CreatePostUiState,
    onLocationNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationSelect: (LatLng) -> Unit,
    onImageClick: () -> Unit,
    onPostClick: () -> Unit,
    availableActivities: List<UiActivity>,
    onSearch: (LatLng) -> Unit
) {
    val scrollState = rememberScrollState()
    var isMapInteracting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 5f)
    }

    var showSearchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState, enabled = !isMapInteracting),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.share_travel_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                    Text(
                        stringResource(R.string.add_photo_label),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.locationName,
            onValueChange = onLocationNameChange,
            label = { Text("Nom du lieu ou ville") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isLoading,
            trailingIcon = {
                IconButton(onClick = {
                    onSearch(cameraPositionState.position.target)
                    showSearchDialog = true
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher un lieu")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Localisez le lieu sur la carte :", style = MaterialTheme.typography.labelMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            isMapInteracting = event.changes.any { it.pressed }
                        }
                    }
                }
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { onLocationSelect(it) }
            ) {
                uiState.selectedLocation?.let {
                    Marker(state = rememberUpdatedMarkerState(it))
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onPostClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = (!uiState.isLoading && uiState.locationName.isNotBlank() && uiState.selectedImageUri != null && uiState.selectedLocation != null),
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

    if (showSearchDialog) {
        LocationSearchDialog(
            availableLocations = availableActivities,
            onDismiss = { showSearchDialog = false },
            onSelect = { activity ->
                val latLng = LatLng(activity.latitude ?: 0.0, activity.longitude ?: 0.0)
                onLocationNameChange(activity.name)
                onLocationSelect(latLng)
                coroutineScope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
                showSearchDialog = false
            }
        )
    }
}

@Composable
fun LocationSearchDialog(
    availableLocations: List<UiActivity>,
    onDismiss: () -> Unit,
    onSelect: (UiActivity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLocations = remember(availableLocations, searchQuery) {
        if (searchQuery.isBlank()) availableLocations
        else availableLocations.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rechercher un lieu") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Nom du lieu...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    LazyColumn {
                        items(filteredLocations) { location ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        location.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                supportingContent = { Text(location.category) },
                                modifier = Modifier.clickable { onSelect(location) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@Composable
fun CreatePostScreen(viewModel: FeedViewModel, onPostSuccess: () -> Unit) {
    var uiState by remember { mutableStateOf(CreatePostUiState()) }
    val availableActivities by viewModel.globalActivities.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uiState = uiState.copy(selectedImageUri = uri)
    }

    CreatePostContent(
        uiState = uiState,
        onLocationNameChange = { uiState = uiState.copy(locationName = it) },
        onDescriptionChange = { uiState = uiState.copy(description = it) },
        onLocationSelect = { uiState = uiState.copy(selectedLocation = it) },
        onImageClick = { launcher.launch("image/*") },
        onPostClick = {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModel.uploadPost(
                location = uiState.locationName,
                description = uiState.description,
                imageUri = uiState.selectedImageUri,
                lat = uiState.selectedLocation?.latitude,
                lon = uiState.selectedLocation?.longitude,
                onSuccess = {
                    uiState = uiState.copy(isLoading = false)
                    onPostSuccess()
                },
                onError = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error)
                }
            )
        },
        availableActivities = availableActivities,
        onSearch = { center ->
            viewModel.searchActivitiesNearby(center.latitude, center.longitude)
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
                onLocationNameChange = {},
                onDescriptionChange = {},
                onLocationSelect = {},
                onImageClick = {},
                onPostClick = {},
                availableActivities = emptyList(),
                onSearch = {}
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
                uiState = CreatePostUiState(isLoading = true, locationName = "Paris"),
                onLocationNameChange = {},
                onDescriptionChange = {},
                onLocationSelect = {},
                onImageClick = {},
                onPostClick = {},
                availableActivities = emptyList(),
                onSearch = {}
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
                    locationName = "Lyon"
                ),
                onLocationNameChange = {},
                onDescriptionChange = {},
                onLocationSelect = {},
                onImageClick = {},
                onPostClick = {},
                availableActivities = emptyList(),
                onSearch = {}
            )
        }
    }
}
