package fr.troubidoo.travelpascher.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.troubidoo.travelpascher.R
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import kotlinx.coroutines.delay

data class SettingsUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val preferredCategories: List<String> = emptyList(),
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

@Composable
fun SettingsScreen(viewModel: FeedViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val userData by viewModel.userData.collectAsState()
    val isUsernameAvailable by viewModel.isUsernameAvailable.collectAsState()

    var settingsState by remember(userData) {
        mutableStateOf(
            SettingsUiState(
                username = userData?.username ?: "",
                firstName = userData?.firstName ?: "",
                lastName = userData?.lastName ?: "",
                bio = userData?.bio ?: "",
                preferredCategories = userData?.preferredCategories ?: emptyList()
            )
        )
    }

    LaunchedEffect(settingsState.username) {
        if (settingsState.username != userData?.username) {
            delay(500)
            viewModel.checkUsernameAvailability(settingsState.username)
        } else {
            viewModel.resetUsernameAvailability()
        }
    }

    val successMessage = stringResource(R.string.profile_updated_success)
    val scrollState = rememberScrollState()

    val categoryMap = mapOf(
        "tourist_attraction" to "Attractions",
        "museum" to "Musées",
        "park" to "Parcs",
        "restaurant" to "Restos",
        "cafe" to "Cafés",
        "lodging" to "Hôtels",
        "shopping_mall" to "Shopping"
    )

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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        val usernameColor = when {
            settingsState.username == userData?.username -> MaterialTheme.colorScheme.outline
            isUsernameAvailable == true -> Color(0xFF4CAF50)
            isUsernameAvailable == false -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.outline
        }

        OutlinedTextField(
            value = settingsState.username,
            onValueChange = { settingsState = settingsState.copy(username = it) },
            label = { Text(stringResource(R.string.username_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !settingsState.isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = usernameColor,
                unfocusedBorderColor = usernameColor,
                focusedLabelColor = usernameColor,
                unfocusedLabelColor = usernameColor
            ),
            supportingText = {
                if (settingsState.username != userData?.username && isUsernameAvailable != null) {
                    Text(
                        text = if (isUsernameAvailable == true) "Pseudo disponible" else "Pseudo déjà utilisé",
                        color = usernameColor
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Préférences d'activités",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Quels types de lieux préférez-vous visiter ?",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryMap.forEach { (type, label) ->
                val isSelected = settingsState.preferredCategories.contains(type)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newList = if (isSelected) {
                            settingsState.preferredCategories - type
                        } else {
                            settingsState.preferredCategories + type
                        }
                        settingsState = settingsState.copy(preferredCategories = newList)
                    },
                    label = { Text(label) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }

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
                    username = settingsState.username,
                    firstName = settingsState.firstName,
                    lastName = settingsState.lastName,
                    bio = settingsState.bio,
                    preferredCategories = settingsState.preferredCategories,
                    newProfileImageUri = settingsState.selectedImageUri,
                    onSuccess = {
                        settingsState = settingsState.copy(
                            isLoading = false,
                            message = successMessage,
                            selectedImageUri = null
                        )
                        onBack()
                    },
                    onError = {
                        settingsState = settingsState.copy(isLoading = false, message = it)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !settingsState.isLoading &&
                    settingsState.firstName.isNotBlank() &&
                    settingsState.lastName.isNotBlank() &&
                    settingsState.username.isNotBlank() &&
                    (settingsState.username == userData?.username || isUsernameAvailable == true)
        ) {
            if (settingsState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.save_profile_button))
            }
        }
    }
}
