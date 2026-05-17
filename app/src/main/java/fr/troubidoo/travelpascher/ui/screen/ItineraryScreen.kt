package fr.troubidoo.travelpascher.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import fr.troubidoo.travelpascher.ui.theme.TravelPasCherTheme
import fr.troubidoo.travelpascher.viewmodel.FeedViewModel
import fr.troubidoo.travelpascher.viewmodel.UiActivity
import fr.troubidoo.travelpascher.viewmodel.UiItinerary
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ItineraryScreen(viewModel: FeedViewModel) {
    val itineraries by viewModel.itineraries.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val discoveryActivities by viewModel.globalActivities.collectAsState()

    var showAddItineraryDialog by remember { mutableStateOf(false) }
    var selectedItineraryForActivity by remember { mutableStateOf<UiItinerary?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedItineraryForActivity) {
        selectedItineraryForActivity?.let { itinerary ->
            if (itinerary.latitude != null && itinerary.longitude != null) {
                viewModel.searchActivitiesNearby(itinerary.latitude, itinerary.longitude)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddItineraryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un parcours")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Découvrir des Parcours",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchItineraries(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Chercher par destination...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (itineraries.isEmpty() && !isRefreshing) {
                    EmptyItineraryView()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(itineraries) { itinerary ->
                            val isOwner = itinerary.userId == currentUser?.uid
                            ItineraryCard(
                                itinerary = itinerary,
                                isOwner = isOwner,
                                onDelete = { viewModel.deleteItinerary(itinerary.id) },
                                onAddActivity = { selectedItineraryForActivity = itinerary },
                                onDeleteActivity = { activity ->
                                    viewModel.deleteActivityFromItinerary(
                                        itinerary.id,
                                        activity
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showAddItineraryDialog) {
        AddItineraryDialog(
            onDismiss = { showAddItineraryDialog = false },
            onConfirm = { title, desc, dest, lat, lon, start, end, isSmart ->
                if (isSmart) {
                    viewModel.generateSmartItinerary(title, desc, dest, lat, lon, start, end)
                } else {
                    viewModel.addItinerary(title, desc, dest, emptyList(), lat, lon, start, end)
                }
                showAddItineraryDialog = false
            }
        )
    }

    selectedItineraryForActivity?.let { itinerary ->
        AddActivityDialog(
            itinerary = itinerary,
            availableActivities = discoveryActivities,
            onDismiss = { selectedItineraryForActivity = null },
            onConfirm = { activity ->
                viewModel.addActivityToItinerary(itinerary.id, activity)
                selectedItineraryForActivity = null
            },
            onSearch = { type ->
                if (itinerary.latitude != null && itinerary.longitude != null) {
                    viewModel.searchActivitiesNearby(itinerary.latitude, itinerary.longitude, type)
                }
            }
        )
    }
}

@Composable
fun ItineraryCard(
    itinerary: UiItinerary,
    isOwner: Boolean,
    onDelete: () -> Unit,
    onAddActivity: () -> Unit,
    onDeleteActivity: (UiActivity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itinerary.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = itinerary.destination,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (itinerary.startDate.isNotEmpty() || itinerary.endDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${itinerary.startDate} - ${itinerary.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = itinerary.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activités",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (isOwner) {
                    TextButton(onClick = onAddActivity) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter une activité")
                    }
                }
            }

            if (itinerary.activities.isEmpty()) {
                Text(
                    text = "Aucune activité pour le moment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                itinerary.activities.forEach { activity ->
                    ActivityItem(
                        activity,
                        isOwner = isOwner,
                        onDelete = { onDeleteActivity(activity) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: UiActivity, isOwner: Boolean, onDelete: () -> Unit) {
    val icon = when (activity.category.lowercase()) {
        "restaurant", "food", "café" -> Icons.Default.Restaurant
        "musée", "culture", "museum" -> Icons.Default.Museum
        "parc", "nature" -> Icons.Default.Park
        "hôtel", "logement" -> Icons.Default.Hotel
        else -> Icons.Default.Place
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            activity.price?.let {
                Text(
                    text = if (it == 0.0) "Gratuit" else "${it}€",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Text(activity.category, style = MaterialTheme.typography.labelSmall)
        }
        if (isOwner) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Supprimer l'activité",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddActivityDialog(
    itinerary: UiItinerary,
    availableActivities: List<UiActivity>,
    onDismiss: () -> Unit,
    onConfirm: (UiActivity) -> Unit,
    onSearch: (String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }

    val categoryMap = mapOf(
        null to "Tous",
        "tourist_attraction" to "Attractions",
        "museum" to "Musées",
        "park" to "Parcs",
        "restaurant" to "Restos",
        "cafe" to "Cafés",
        "lodging" to "Hôtels"
    )

    val displayedActivities = remember(availableActivities, searchQuery) {
        if (searchQuery.isBlank()) {
            availableActivities
        } else {
            availableActivities.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Rechercher une activité")
                Spacer(modifier = Modifier.height(8.dp))
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryMap.toList()) { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                onSearch(type)
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                if (displayedActivities.isEmpty()) {
                    Text("Aucun résultat pour cette recherche.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(displayedActivities) { activity ->
                            val distance =
                                if (itinerary.latitude != null && itinerary.longitude != null && activity.latitude != null && activity.longitude != null) {
                                    calculateDistance(
                                        itinerary.latitude,
                                        itinerary.longitude,
                                        activity.latitude,
                                        activity.longitude
                                    )
                                } else null

                            ListItem(
                                headlineContent = {
                                    Text(
                                        activity.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                supportingContent = {
                                    Text("${activity.category} • ${if (activity.price == 0.0) "Gratuit" else "${activity.price}€"}")
                                },
                                trailingContent = {
                                    if (distance != null) {
                                        Text(
                                            "${distance.toInt()} km",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { onConfirm(activity) }
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

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@Composable
fun AddItineraryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double?, Double?, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isSmartMode by remember { mutableStateOf(false) }
    var isMapInteracting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 10f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Parcours") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(scrollState, enabled = !isMapInteracting)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre du voyage") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dest,
                    onValueChange = { dest = it },
                    label = { Text("Destination (Ville)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Début (JJ/MM)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Fin (JJ/MM)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSmartMode, onCheckedChange = { isSmartMode = it })
                    Text(
                        "Générer intelligemment (ajoute des activités)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    "Cliquez sur la carte pour définir le lieu :",
                    style = MaterialTheme.typography.labelMedium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                        onMapClick = { selectedLocation = it }
                    ) {
                        selectedLocation?.let {
                            Marker(
                                state = rememberUpdatedMarkerState(it),
                                title = "Lieu sélectionné"
                            )
                        }
                    }
                }

                if (selectedLocation != null) {
                    Text(
                        text = "Coordonnées : ${
                            String.format(
                                Locale.ROOT,
                                "%.4f",
                                selectedLocation?.latitude
                            )
                        }, ${String.format(Locale.ROOT, "%.4f", selectedLocation?.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title,
                        desc,
                        dest,
                        selectedLocation?.latitude,
                        selectedLocation?.longitude,
                        startDate,
                        endDate,
                        isSmartMode
                    )
                },
                enabled = title.isNotBlank() && dest.isNotBlank() && selectedLocation != null
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun EmptyItineraryView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aucun parcours pour le moment",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Appuyez sur le bouton + pour commencer à planifier votre prochain voyage !",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ItineraryScreenPreview() {
    val sampleActivities = listOf(
        UiActivity(
            id = "1",
            name = "Tour Eiffel",
            description = "Visite du monument",
            location = "Paris",
            category = "Musée",
            price = 25.0,
            latitude = 48.8584,
            longitude = 2.2945
        ),
        UiActivity(
            id = "2",
            name = "Boulangerie",
            description = "Petit déjeuner",
            location = "Paris",
            category = "Restaurant",
            price = 8.5,
            latitude = 48.8631,
            longitude = 2.3670
        )
    )

    val sampleItineraries = listOf(
        UiItinerary(
            id = "it1",
            userId = "user1",
            title = "Weekend à Paris",
            description = "Petit séjour romantique dans la capitale.",
            destination = "Paris",
            createdAt = System.currentTimeMillis(),
            startDate = "12/06",
            endDate = "14/06",
            activities = sampleActivities,
            latitude = 48.8566,
            longitude = 2.3522
        ),
        UiItinerary(
            id = "it2",
            userId = "user1",
            title = "Escapade à Londres",
            description = "Shopping et fish & chips.",
            destination = "London",
            createdAt = System.currentTimeMillis() - 86400000,
            startDate = "20/07",
            endDate = "22/07",
            activities = emptyList(),
            latitude = 51.5074,
            longitude = -0.1278
        )
    )

    TravelPasCherTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Mes Parcours (Preview)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sampleItineraries) { itinerary ->
                    ItineraryCard(
                        itinerary = itinerary,
                        isOwner = true,
                        onDelete = {},
                        onAddActivity = {},
                        onDeleteActivity = {}
                    )
                }
            }
        }
    }
}
