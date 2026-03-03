package com.example.civicconnectai.addissue

import CivicIssue
import android.Manifest
import android.R.attr.icon
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.civicconnectai.SupabaseManager
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    existingCategory: String = "Select Issue Type",
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit
) {

    // 1. Create the Focus and Keyboard controllers
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current



    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    // State variables (Initialized with passed data)
    var title by remember {mutableStateOf( "")}
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(existingCategory) }
    var expandedCategory by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

         // Image State: Can be a Uri (Gallery) OR a Bitmap (Camera)
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }


         // Dropdown options
        val categories = listOf("Pothole", "Streetlight", "Graffiti", "Trash", "Other")

    //  Popup Map state
    var locationText by remember { mutableStateOf("") }
    var locationLat by remember { mutableStateOf(0.0) }
    var locationLng by remember { mutableStateOf(0.0) }
    var showMapPopup by remember { mutableStateOf(false) }

    // State to control if the text field can be typed in
    var isLocationEditable by remember { mutableStateOf(false) }


    if (showMapPopup) {
        MapLocationPickerDialog(
            onDismiss = {
                showMapPopup = false // Close without saving
            },
            onLocationConfirmed = { address , lat , lng  ->
                locationText = address
                locationLat = lat
                locationLng = lng
                showMapPopup = false  // Close the map
            },

        )
    }


    // --- LAUNCHERS ---

    // 1. Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            imageBitmap = null
        }
    }

    // 2. Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imageBitmap = bitmap
            imageUri = null
        }
    }

    // 3. Permission Launcher (For Camera)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        // Dynamic Title based on mode
                        text = "Issue Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SectionTitle("UPLOAD PHOTO EVIDENCE")

            // --- IMAGE PREVIEW BOX ---
            Box(
                modifier = Modifier
                    .padding(horizontal = 25.dp)
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Color(0xFF5B75E6),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Image Selected", color = Color.Gray)
                    }
                }
            }

            // --- ACTION BUTTONS ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. CAMERA BUTTON
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp) ,
                        tint = Color(0xFF5B75E6)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }

                // 2. GALLERY BUTTON
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp) ,
                        tint = Color(0xFF5B75E6)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---  Title Section ---
            SectionTitle("TITLE")

            Card(
                modifier = Modifier
                    .fillMaxWidth() ,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it } , // Prevent change if not editable
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null,
                            tint = Color(0xFF5B75E6)
                        )
                    },
                    placeholder = {

                            Text(
                                "Title ...",
                                color = Color.Gray
                            )
                    },
                    modifier = Modifier.fillMaxSize().padding(start = 10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Category Section ---
            SectionTitle("CATEGORY")

            Box {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Only clickable if editable
                        .clickable(enabled = true) { expandedCategory = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color(0xFF5B75E6)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = selectedCategory,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedCategory == "Select Issue Type") Color.Gray else Color.Black,
                            modifier = Modifier.weight(1f)
                        )

                        // Hide dropdown arrow if not editable

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }

                // Dropdown Menu Logic
                DropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Details Section ---
            SectionTitle("DETAILS")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TextField(
                    value = description,
                    onValueChange = { description = it }, // Prevent change if not editable
                    leadingIcon = {
                        Box(
                            // 1. Stretch the box to the full 120dp height
                            modifier = Modifier.fillMaxHeight().padding(top = 16.dp),
                            // 2. Pin the icon to the top center!
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF5B75E6)
                            )
                        }
                    },
                    placeholder = {

                            Text(
                                "Please describe the issue in detail...",
                                color = Color.Gray
                            )
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Location Section ---
            SectionTitle("LOCATION")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Map Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFFE0E0E0))
                    )
                    {
                        IconButton(
                            onClick = { showMapPopup = true} ,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFFE0E0E0))
                        )

                        {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF5B75E6),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (-10).dp)
                                    .size(40.dp)
                            )
                        }
                    }
                    //  Watch for changes to `isLocationEditable`
                    LaunchedEffect(isLocationEditable) {
                        if (isLocationEditable) {
                            // Give it a tiny delay to ensure the TextField is completely unlocked first
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } else {
                            // Hide keyboard when they click "Check" (Done)
                            keyboardController?.hide()
                        }
                    }

                    // Address Row
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color(0xFF5B75E6),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AUTO-CAPTURED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF5B75E6),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = locationText,
                                onValueChange = { locationText = it },
                                // When isLocationEditable is false, readOnly is true (Locked)
                                // When isLocationEditable is true, readOnly is false (You can type)
                                readOnly = !isLocationEditable,

                                trailingIcon = {
                                    if (isLocationEditable) {
                                        // SHOW DONE BUTTON (User is currently typing)
                                        IconButton(
                                            onClick = {
                                                isLocationEditable = false // Lock the field when done
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Done Editing",
                                                tint = MaterialTheme.colorScheme.primary // Make it pop visually
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(0.dp).background(Color.White) ,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                )
                            )
                        }

                        IconButton(
                            onClick = { isLocationEditable = true },
                            modifier = Modifier
                                .background(Color(0xFFF8F9FA), shape = RoundedCornerShape(50))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. Submit Button  ---
            Button(
                onClick = {// 1. Check if Image is missing (Neither URI nor Bitmap exists)
                    if (imageUri == null && imageBitmap == null) {
                        Toast.makeText(
                            context,
                            "Please upload an image evidence.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                        else if (title.isBlank())
                    {
                        Toast.makeText(context, "Please Enter title. ", Toast.LENGTH_SHORT)
                            .show()
                        }
                    // 2. Check Text Fields
                    else if (selectedCategory == "Select Issue Type") {
                        Toast.makeText(context, "Please enter an issue title.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else if (locationLng == 0.0 && locationLat == 0.0) {
                        Toast.makeText(context, "Please select location on map", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else if (locationText.isBlank()) {
                        Toast.makeText(context, "Please enter the location text.", Toast.LENGTH_SHORT)
                            .show()
                    } else if (description.isBlank()) {
                        Toast.makeText(context, "Please enter a description.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // 3. Success -> Submit
                    else {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            Toast.makeText(
                                context,
                                "You must be logged in to report an issue",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        isSubmitting = true

                        // Now 'scope' perfectly resolves!
                        scope.launch {
                            try {
                                val databaseRef =
                                    FirebaseDatabase.getInstance().getReference("issues")
                                val newIssueId = databaseRef.push().key ?: return@launch

                                var finalImageUrl = ""
                                if (imageUri != null) {
                                    val uploadedUrl =
                                        uploadImageToSupabase(
                                            context, imageUri!!, imageBitmap , newIssueId
                                        )
                                    if (uploadedUrl != null) {
                                        finalImageUrl = uploadedUrl
                                    }
                                    else{
                                        Toast.makeText(
                                            context,
                                            "Issue not Reported",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                val newIssue = CivicIssue(
                                    issueId = newIssueId,
                                    userId = currentUser.uid,
                                    title = title ,
                                    category = selectedCategory,
                                    description = description,
                                    latitude = locationLat,
                                    longitude = locationLng,
                                    address = locationText,
                                    imageUrl = finalImageUrl,
                                    status = "Pending",
                                    timestamp = System.currentTimeMillis() ,
                                    votevalid = 0L,
                                    voteinvalid = 0L,
                                )

                                databaseRef.child(newIssueId).setValue(newIssue)
                                    .addOnSuccessListener {
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            "Issue reported successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onSubmitClick()
                                    }
                                    .addOnFailureListener { e ->
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            "Failed to submit: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                            } catch (e: Exception) {
                                isSubmitting = false
                                Toast.makeText(
                                    context,
                                    "Error: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp) ,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                else {
                    Text(
                        text = "Submit Complaint",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

        }
    }
}
// --- Helper Components ---

    @Composable
    fun SectionTitle(title: String) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
    }

    @Composable
    fun EvidenceButton(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onClick() }
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFEEF2FF),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF5B75E6)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

suspend fun uploadImageToSupabase(
    context: Context,
    imageUri: Uri?,
    imageBitmap: Bitmap?,
    issueId: String // e.g., the unique ID of the report
): String? {
    return withContext(Dispatchers.IO) {
        try {
            // 1. Get the Firebase User UID
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Log.e("SupabaseUpload", "User not logged in")
                return@withContext null
            }
            val uid = user.uid

            // 2. Convert EITHER the Uri or the Bitmap into a ByteArray
            val byteArray: ByteArray? = when {
                imageUri != null -> {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    bytes
                }
                imageBitmap != null -> {
                    val stream = ByteArrayOutputStream()
                    // Compress the camera bitmap to JPEG format (100% quality)
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.toByteArray()
                }
                else -> null
            }

            if (byteArray == null) {
                Log.e("SupabaseUpload", "Could not read image data")
                return@withContext null
            }

            // 3. Create a unique path: "UID/issueId_timestamp.jpg"
            // This groups all images by the user who uploaded them
            val fileName = "$uid/${issueId}.jpg"

            // 4. Upload to the bucket
                val bucket = SupabaseManager.client.storage.from("civicconnectai")
            bucket.upload(path = fileName, data = byteArray) {
                upsert = false
            }

            // 5. Get the Public URL so you can save it to your Firebase Realtime Database
            val publicUrl = bucket.publicUrl(fileName)
            return@withContext publicUrl

        } catch (e: Exception) {
            Log.e("SupabaseUpload", "Upload failed: ${e.message}")
            return@withContext null
        }
    }

}



// --- Preview for Editing ---
@Preview(showBackground = true)
@Composable
fun ReportIssueScreenEditPreview() {
    CivicConnectTheme {
        ReportIssueScreen( onBackClick = {}, onSubmitClick = {})
    }
}
