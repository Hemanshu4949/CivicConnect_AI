package com.example.civicconnectai.bottomNavScreens

import android.Manifest
import android.R.attr.onClick
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    reportIssueScreen: () -> Unit
) {
    val context = LocalContext.current

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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val sanFrancisco = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sanFrancisco, 12f)
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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = isLocationPermissionGranted
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                Marker(state = MarkerState(position = LatLng(37.7849, -122.4294)), title = "Critical Issue")
                Marker(state = MarkerState(position = LatLng(37.7649, -122.4094)), title = "Resolved Issue")
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
                MapFilterChip(label = "All Issues", selected = true, color = Color(0xFF5B75E6))
                MapFilterChip(label = "Critical", selected = false, color = Color(0xFFD32F2F))
                MapFilterChip(label = "Resolved", selected = false, color = Color(0xFF388E3C))
                MapFilterChip(label = "My Reports", selected = false, color = Color.Gray)
            }

            // LAYER C: Controls (Zoom/Location)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MapControlButton(icon = Icons.Default.Add ,
                    onClick = {
                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                })
                MapControlButton(icon = Icons.Default.Remove ,
                    onClick = {
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                    }
                    )
                MapControlButton(icon = Icons.Default.MyLocation ,
                    onClick = {
                        if (isLocationPermissionGranted) {
                            try {
                                @SuppressLint("MissingPermission") // We checked permission above
                                val locationResult = fusedLocationClient.lastLocation
                                locationResult.addOnCompleteListener { task ->
                                    if (task.isSuccessful && task.result != null) {
                                        val userLocation = LatLng(task.result.latitude, task.result.longitude)
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(userLocation, 15f)
                                            )
                                        }
                                    }
                                }
                            } catch (e: SecurityException) {
                                // Handle exception if permission revoked
                            }
                        } else {
                            // Ask for permission again if they clicked but didn't grant it
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }

                    }
                )
            }

            // LAYER D: Floating Action Button
            FloatingActionButton(
                onClick = { reportIssueScreen() },
                containerColor = Color(0xFF5B75E6),
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Report Issue",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun MapFilterChip(label: String, selected: Boolean, color: Color) {
    val backgroundColor = if (selected) color else Color.White
    val textColor = if (selected) Color.White else Color.Black
    val borderColor = if (selected) Color.Transparent else Color.LightGray

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        modifier = Modifier
            .height(36.dp)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .shadow(if (selected) 4.dp else 2.dp, RoundedCornerShape(50))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
        }
    }
}

@Composable
fun MapControlButton(icon: ImageVector,
                     onClick: () -> Unit) {
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

@Preview
@Composable
fun MapScreenPreview() {
    CivicConnectTheme {
        MapScreen(reportIssueScreen = {})
    }
}