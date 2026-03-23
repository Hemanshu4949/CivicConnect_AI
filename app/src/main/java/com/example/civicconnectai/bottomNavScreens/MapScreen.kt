package com.example.civicconnectai.bottomNavScreens

import CivicIssue
import IssueViewModel
import android.Manifest
import android.R.attr.onClick
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.civicconnectai.addissue.checkGpsAndFetchLocation
import com.example.civicconnectai.addissue.fetchCurrentLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: IssueViewModel,

    reportIssueScreen: () -> Unit ,
    onIssueClick: (String) -> Unit
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    // get's the selected chip value
    var mapSelectedStatus by remember { mutableStateOf("All Issues") }

    val issuesList by viewModel.issuelist.collectAsState()

    // 3. NEW: A locally filtered list just for the map pins
    val mapPins = remember(issuesList, mapSelectedStatus) {
        issuesList.filter { issue ->
            when (mapSelectedStatus) {
                "All Issues" -> true
                "Pending" -> issue.status == "Pending"
                "In Progress" -> issue.status == "In Progress"
                "Resolved" -> issue.status == "Resolved"
                "My Reports" -> {val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                    // Only show the pin if the user is logged in AND their ID matches the issue's ID
                    currentUserId != null && issue.userId == currentUserId}
                else -> true
            }
        }
    }

    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }


    // Permission Logic
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }



    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.1702, 72.8311), 12f)
    }

    // slide for min detail about issue
    var selectedIssue by remember { mutableStateOf<CivicIssue?>(null) }
    var showIssueSheet by remember { mutableStateOf(false) }




// permission launcher
    // ADD THIS LAUNCHER: It listens for the user to click "OK" on the GPS popup
    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // The user clicked "OK" and GPS is now ON!
            fetchCurrentLocation(context, fusedLocationClient) { latLng ->
                coroutineScope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
        } else {
            Toast.makeText(context, "GPS is required to find your location", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        isLocationPermissionGranted = isGranted
        if (isGranted) {
            // Permission granted! Fetch the location
            checkGpsAndFetchLocation(context, gpsSettingLauncher) {
                try {
                    @SuppressLint("MissingPermission")
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(userLocation, 16f)
                                )
                            }
                        } else {
                            // Fallback just in case they are completely underground!
                            Toast.makeText(
                                context,
                                "Searching for GPS signal...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: SecurityException) {
                    // Handled securely
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }



    LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
            // 1. Permission not granted yet? Ask for it!
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // 2. Permission ALREADY granted? Move the camera immediately!
            checkGpsAndFetchLocation(context, gpsSettingLauncher) {
                fetchCurrentLocation(context, fusedLocationClient) { latLng ->
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    }
                }
            }
        }
    }


    // --- MAIN LAYOUT: COLUMN ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
        // Pushes content below the system status bar
    ) {

        // 1. TITLE SECTION (Outside the Map)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Map View",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        // 2. MAP CONTAINER (Box allows layering)
        Box(
            modifier = Modifier
                .weight(1f) // Fills all remaining screen space
                .fillMaxWidth()
        ) {
            // LAYER A: The Google Map (Background)
            val mapProperties = remember(isLocationPermissionGranted) {
                MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                mapPins.forEach { issue ->
                    val lat = issue.latitude
                    val lng = issue.longitude

                    // Only plot issues that actually have valid GPS coordinates
                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            icon = getCategoryMarkerIcon(issue.category),
                            onClick = { marker ->
                                selectedIssue = issue
                                showIssueSheet = true
                                true // Returning 'true' tells Google Maps: "I handled the click, don't show the default text popup."
                            }
                        )
                    }
                }
            }
                // LAYER B: Filter Chips (Floating ON TOP of Map)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter) // Pinned to top of Map
                        .fillMaxWidth()
                        .padding(top = 16.dp) // Spacing from the Title
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapFilterChip(label = "All Issues", selected = mapSelectedStatus == "All Issues", color = Color(0xFF5B75E6) , onClick = {mapSelectedStatus = "All Issues"})
                    MapFilterChip(label = "Pending", selected = mapSelectedStatus == "Pending", color = Color(0xFFD32F2F), onClick = {mapSelectedStatus = "Pending"})
                    MapFilterChip(
                        label = "In Progress",
                        selected = mapSelectedStatus == "In Progress",
                        color = Color(0xFFFFE600),
                        onClick = {mapSelectedStatus = "In Progress"}
                    )
                    MapFilterChip(label = "Resolved", selected = mapSelectedStatus == "Resolved", color = Color(0xFF388E3C) ,  onClick = {mapSelectedStatus = "Resolved"})
                    MapFilterChip(label = "My Reports", selected = mapSelectedStatus == "My Reposts", color = Color.Gray, onClick = {mapSelectedStatus = "My Reposts"})
                }

                // LAYER C: Controls (Zoom/Location)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MapControlButton(
                        icon = Icons.Default.Add,
                        onClick = {
                            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                        })
                    MapControlButton(
                        icon = Icons.Default.Remove,
                        onClick = {
                            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                        }
                    )
                    MapControlButton(
                        icon = Icons.Default.MyLocation,
                        onClick = {
                            if (isLocationPermissionGranted) {
                                try {
                                    @SuppressLint("MissingPermission") // We checked permission above
                                    val locationResult = fusedLocationClient.lastLocation
                                    locationResult.addOnCompleteListener { task ->
                                        if (task.isSuccessful && task.result != null) {
                                            val userLocation =
                                                LatLng(task.result.latitude, task.result.longitude)
                                            scope.launch {
                                                cameraPositionState.animate(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        userLocation,
                                                        15f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    // Handle exception if permission revoked
                                }
                            } else {
                                // Ask for permission again if they clicked but didn't grant it
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                        }
                    )
                }

//                // LAYER D: Floating Action Button
//                FloatingActionButton(
//                    onClick = { reportIssueScreen() },
//                    containerColor = Color(0xFF5B75E6),
//                    contentColor = Color.White,
//                    shape = RoundedCornerShape(50),
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(16.dp)
//                        .size(64.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Report Issue",
//                        modifier = Modifier.size(32.dp)
//                    )
//                }
                // for showing bottom card view
                if (showIssueSheet && selectedIssue != null) {
                    val issue = selectedIssue!! // Safe to unwrap because we checked for null

                    ModalBottomSheet(
                        onDismissRequest = { showIssueSheet = false },
                        containerColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, bottom = 32.dp)
                        ) {

                            // 1. THE IMAGE IS SAFE HERE!
                            if (!issue.imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(issue.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Issue Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.LightGray)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // 2. The Details
                            Text(
                                text = issue.title ?: "Reported Issue",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${issue.category} • ${issue.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // 3. Navigate to Full Detail Screen
                            Button(
                                onClick = {
                                    showIssueSheet = false
                                    issue.issueId?.let { id -> onIssueClick(id) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B75E6))
                            ) {
                                Text("View Full Details" , color = Color.White)
                            }
                        }
                    }
                }
            }
        }
}

// --- Helper Composables ---

    @Composable
    fun MapFilterChip(label: String, selected: Boolean, color: Color , onClick: () -> Unit) {
        val backgroundColor = if (selected) color else Color.White
        val textColor = if (selected) Color.White else Color.Black
        val borderColor = if (selected) Color.Transparent else Color.LightGray

        Surface(
            shape = RoundedCornerShape(50),
            color = backgroundColor,
            onClick = onClick,
            modifier = Modifier
                .height(36.dp)
                .border(1.dp, borderColor, RoundedCornerShape(50))
                .shadow(if (selected) 4.dp else 2.dp, RoundedCornerShape(50))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }

    @Composable
    fun MapControlButton(
        icon: ImageVector,
        onClick: () -> Unit
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape),
            onClick = onClick
        )
        {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
            }
        }
    }

    fun getCategoryMarkerIcon(category: String?): BitmapDescriptor {
        val hue = when (category) {
            "Pothole" -> BitmapDescriptorFactory.HUE_ORANGE
            "Streetlight" -> BitmapDescriptorFactory.HUE_AZURE   // Light Blue
            "Graffiti" -> BitmapDescriptorFactory.HUE_YELLOW
            "Trash" -> BitmapDescriptorFactory.HUE_GREEN
            else -> BitmapDescriptorFactory.HUE_RED        // Default color
        }
        return BitmapDescriptorFactory.defaultMarker(hue)
    }