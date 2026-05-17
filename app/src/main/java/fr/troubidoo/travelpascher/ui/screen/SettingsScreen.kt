package fr.troubidoo.travelpascher.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel

data class SettingsUiState(
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

@Composable
fun SettingsScreen(viewModel: FeedViewModel, onBack: () -> Unit) {
    val userData by viewModel.userData.collectAsState()
    var settingsState by remember(userData) {
        mutableStateOf(
            SettingsUiState(
                firstName = userData?.firstName ?: "",
                lastName = userData?.lastName ?: "",
                bio = userData?.bio ?: ""
            )
        )
    }
    
    val successMessage = stringResource(R.string.profile_updated_success)
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        settingsState = settingsState.copy(selectedImageUri = uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Zone de sélection de photo de profil
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(enabled = !settingsState.isLoading) { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (settingsState.selectedImageUri != null) {
                AsyncImage(
                    model = settingsState.selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!userData?.profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = userData?.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = settingsState.firstName,
            onValueChange = { settingsState = settingsState.copy(firstName = it) },
            label = { Text(stringResource(R.string.firstname_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !settingsState.isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = settingsState.lastName,
            onValueChange = { settingsState = settingsState.copy(lastName = it) },
            label = { Text(stringResource(R.string.lastname_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !settingsState.isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = settingsState.bio,
            onValueChange = { settingsState = settingsState.copy(bio = it) },
            label = { Text(stringResource(R.string.bio_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !settingsState.isLoading
        )

        if (settingsState.message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val isSuccess = settingsState.message == successMessage
            Text(
                text = settingsState.message!!,
                color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                settingsState = settingsState.copy(isLoading = true)
                viewModel.updateUserProfile(
                    firstName = settingsState.firstName,
                    lastName = settingsState.lastName,
                    bio = settingsState.bio,
                    newProfileImageUri = settingsState.selectedImageUri,
                    onSuccess = {
                        settingsState = settingsState.copy(
                            isLoading = false,
                            message = successMessage,
                            selectedImageUri = null // Reset selection after success
                        )
                    },
                    onError = {
                        settingsState = settingsState.copy(isLoading = false, message = it)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !settingsState.isLoading && settingsState.firstName.isNotBlank() && settingsState.lastName.isNotBlank()
        ) {
            if (settingsState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.save_profile_button))
            }
        }
    }
}
