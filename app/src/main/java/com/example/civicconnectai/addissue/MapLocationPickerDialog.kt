package com.example.civicconnectai.addissue

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
    fun MapLocationPickerDialog(
    initialLocation: LatLng? = null, // ADD THIS: So we can pass the issue's location
    isReadOnly: Boolean = false,     // ADD THIS: To hide the confirm button
        onDismiss: () -> Unit,
        onLocationConfirmed: (String , Double , Double) -> Unit // Returns the text address
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

         val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }


        // Default location (Surat)
        val defaultLocation = LatLng(21.1702, 72.8311)
    var pickedLocation by remember { mutableStateOf(initialLocation) }
    var isFetchingAddress by remember { mutableStateOf(false) }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
        }
    //  Force the camera to jump to the pin instantly!
    LaunchedEffect(initialLocation) {
        if (initialLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(initialLocation, 16f)
        }
    }

// permission launcher
    // ADD THIS LAUNCHER: It listens for the user to click "OK" on the GPS popup
    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // The user clicked "OK" and GPS is now ON!
            fetchCurrentLocation(context, fusedLocationClient) { latLng ->
                if (!isReadOnly) pickedLocation = latLng
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
        hasLocationPermission = isGranted
        if (isGranted) {
            // Permission granted! Fetch the location
            checkGpsAndFetchLocation(context, gpsSettingLauncher) {
                fetchCurrentLocation(context, fusedLocationClient) { latLng ->
                    if (!isReadOnly) pickedLocation = latLng
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    }
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }



    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

        // The Popup Window
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Allows custom width/height
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f) // Take up 95% of screen width
                    .fillMaxHeight(0.8f), // Take up 80% of screen height
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val mapProperties by remember(hasLocationPermission) {
                        mutableStateOf(
                            MapProperties(isMyLocationEnabled = hasLocationPermission)
                        )
                    }

// 2. This hides the default Google Maps target button so your custom FAB shines
                    val mapUiSettings by remember {
                        mutableStateOf(
                            MapUiSettings(
                                myLocationButtonEnabled = false, // We use your custom FAB instead!
                                zoomControlsEnabled = false      // Optional: hide default + / - zoom buttons
                            )
                        )
                    }

                    // 1. The Map
                        GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = mapUiSettings,
                        onMapClick = { latLng ->
                                if (!isReadOnly) pickedLocation = latLng
                            }                    ) {
                        pickedLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Issue Location"
                            )
                        }
                    }

                    // 2. Close Button (Top Right)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Close Map",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 3. Current Location Button (Floating above Confirm button)
                    FloatingActionButton(
                        onClick = {
                            // Check if permission is already granted
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                checkGpsAndFetchLocation(context, gpsSettingLauncher) {
                                    fetchCurrentLocation(context, fusedLocationClient) { latLng ->
                                        if (!isReadOnly) {
                                            pickedLocation = latLng
                                        }
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    latLng,
                                                    16f
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Ask for permission
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 100.dp, end = 5.dp) // Placed just above the confirm area
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
                    }


                    // 4. Confirm Button (Bottom Center)
                   if (pickedLocation != null && !isReadOnly){
                        Button(
                            onClick = {
                                isFetchingAddress = true
                                // Run Geocoder in background so it doesn't freeze the UI
                                coroutineScope.launch {
                                    val addressText = getAddressFromLatLng(context, pickedLocation!!)
                                    onLocationConfirmed(addressText , pickedLocation!!.latitude, pickedLocation!!.longitude)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp),
                            enabled = !isFetchingAddress
                        ) {
                            if (isFetchingAddress) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Confirm Location")
                            }
                        }
                    }
                }
            }
        }
    }

// Helper to safely fetch the last known location
@SuppressLint("MissingPermission") // We safely check permission before calling this
fun fetchCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (LatLng) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            onLocationFetched(LatLng(location.latitude, location.longitude))
        } else {
            Toast.makeText(context, "Turn on device GPS to find location", Toast.LENGTH_LONG).show()
        }
    }
}

    // Helper function to turn GPS coordinates into a street address
    suspend fun getAddressFromLatLng(context: android.content.Context, latLng: LatLng): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Combine the first line of the address with the city
                    "${address.getAddressLine(0)}"
                } else {
                    "${latLng.latitude}, ${latLng.longitude}" // Fallback to raw coordinates
                }
            } catch (e: Exception) {
                "${latLng.latitude}, ${latLng.longitude}" // Fallback on error
            }
        }
    }
fun checkGpsAndFetchLocation(
    context: Context,
    launcher: androidx.activity.compose.ManagedActivityResultLauncher<IntentSenderRequest, androidx.activity.result.ActivityResult>,
    onGpsEnabled: () -> Unit
) {
    // 1. Create a request that demands High Accuracy GPS
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    // 2. Check the device settings
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        // GPS is already ON! Go straight to fetching the location.
        onGpsEnabled()
    }

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            // GPS is OFF, but we can ask Android to show the "Turn On GPS" popup!
            try {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                launcher.launch(intentSenderRequest)
            } catch (sendEx: Exception) {
                // Ignore the error
            }
        }
    }
}