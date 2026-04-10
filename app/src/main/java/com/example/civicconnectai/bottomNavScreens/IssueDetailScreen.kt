package com.example.civicconnectai.bottomNavScreens

import CivicIssue
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.civicconnectai.addissue.MapLocationPickerDialog
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issue: CivicIssue?,
    onBackClick: () -> Unit ,

) {
    val backgroundColor = Color(0xFFF8F9FA)
    val infrastructureColor = Color(0xFFE3F2FD)
    val infrastructureTextColor = Color(0xFF1976D2)
    val highPriorityColor = Color(0xFFFFEBEE)
    val highPriorityTextColor = Color(0xFFD32F2F)

    //map variables
    var showMapDialog by remember { mutableStateOf(false) }

    // voting system variables
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var myCurrentVote by remember { mutableStateOf<String?>(null) }

    // --- ADD THESE TWO LINES HERE ---
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    // 1. The Server State: What does Firebase currently think? (From your DisposableEffect)
    // Let's assume myCurrentVote is "valid" if they upvoted, or null if they haven't.
    val serverHasVoted = (myCurrentVote == "valid")
    var serverVote by remember { mutableStateOf<String?>(null) }




//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(issue?.issueId) {
//        if (currentUserId != null) {
//            val voteRef = FirebaseDatabase.getInstance().getReference("issue_votes")
//                .child(issue?.issueId ?: "none").child(currentUserId)
//
//            // Listen in real-time so the button highlights instantly
//            voteRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
//                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
//                    myCurrentVote = snapshot.getValue(String::class.java)
//                }
//                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
//            })
//        }
//    }

//    DisposableEffect(issue?.issueId) {
//        if (issue?.issueId == null) return@DisposableEffect onDispose {}
////        val userVoteRef = FirebaseDatabase.getInstance().getReference("issuevote").child(issueId).child(currentUserId)
//        val voteRef = FirebaseDatabase.getInstance().getReference("issue_votes").child(issue.issueId)
//
//        val listener = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                var vCount = 0
//                var iCount = 0
//                var myVote: String? = null
//
//                // Loop through every user who voted on this issue
//                for (child in snapshot.children) {
//                    val voteType = child.getValue(String::class.java)
//
//                    if (voteType == "valid") vCount++
//                    if (voteType == "invalid") iCount++
//
//                    // Check if this specific vote belongs to the current logged-in user
//                    if (child.key == currentUserId) {
//                        myVote = voteType
//                    }
//                }
//
//                // Update the UI instantly
//                validCount = vCount
//                invalidCount = iCount
//                myCurrentVote = myVote
//                serverVote = myVote
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        }
//
//        voteRef.addValueEventListener(listener)
//
//        // Automatically remove the listener when the user presses the Back button
//        onDispose {
//            voteRef.removeEventListener(listener)
//        }
//    }

    DisposableEffect(issue?.issueId) {
        val currentIssueId = issue?.issueId ?: return@DisposableEffect onDispose {}
        val userId = currentUserId ?: return@DisposableEffect onDispose {}

        // Point directly to this single user's vote
        val myVoteRef = FirebaseDatabase.getInstance()
            .getReference("issue_votes")
            .child(currentIssueId)
            .child(userId)

        val myVoteListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val voteType = snapshot.getValue(String::class.java)
                    myCurrentVote = voteType
                    serverVote = voteType
                } else {
                    myCurrentVote = null
                    serverVote = null
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        myVoteRef.addValueEventListener(myVoteListener)

        onDispose {
            myVoteRef.removeEventListener(myVoteListener)
        }
    }



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Issue Details",
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
                // No actions block here, so the share button is removed.
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->



        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. Large Image (Non-overlapping) ---
            // In a real app, use AsyncImage (from Coil) here with a URL.
            Box(
                modifier = Modifier
                    .height(250.dp)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(Color.Gray, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
                // Placeholder for the image
            ) {

                if ((issue?.imageUrl?.isNotEmpty() ?: "null") == true) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(issue?.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Issue Image",
                        contentScale = ContentScale.FillBounds, // Crops it perfectly to fit the box
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                } else {
                    // Placeholder if no image was uploaded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image Available", color = Color.DarkGray)
                    }
                }
//                 Image(
//                     painter = painterResource(id = issue.imageUrl ?: 0),
//                     contentDescription = "Issue Image",
//                     contentScale = ContentScale.FillBounds,
//                     modifier = Modifier.fillMaxSize()
//                 )
//                if(issue?.imageUrl == null) {
//                    Text(
//                        "Image of Pothole",
//                        modifier = Modifier.align(Alignment.Center),
//                        color = Color.White
//                    )
//                }
            }

            // Space between image and the first card
            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Main Details Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TagChip(issue?.category ?: "none", infrastructureColor, infrastructureTextColor)
                        TagChip(issue?.status ?: "none", highPriorityColor, highPriorityTextColor)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = issue?.title ?: "none",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#CIV-2023-849",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getTimeAgo(issue?.timestamp ?: 0),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location
                    Row {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF5B75E6),
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEEF2FF), CircleShape)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = issue?.address ?: "none",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "New York, NY 10011",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // ... inside your IssueDetailScreen Column ...

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

// 1. Construct the Google Maps Static URL
// Replace "YOUR_GOOGLE_MAPS_API_KEY" with your actual key!
                    val apiKey = "AIzaSyCZeSw8_NRrgUu1Iiz4f4bVIXd5pZSD5uQ"
                    val lat = issue?.latitude // Assuming you saved these in Firebase!
                    val lng = issue?.longitude
                    val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                            "center=$lat,$lng" +
                            "&zoom=15" +
                            "&size=600x300" +
                            "&maptype=roadmap" +
                            "&markers=color:red%7C$lat,$lng" + // %7C is the URL-encoded '|' character
                            "&key=$apiKey"

                    Log.e("MapDebug", "Map URL: $mapUrl");

// 2. The Interactive Map Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = {
                            showMapDialog = true
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {

                            // 3. The Map Image (Loads instantly!)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(mapUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Map showing issue location",
                                contentScale = ContentScale.Crop, // Stretches the map perfectly to fit the box
                                modifier = Modifier.fillMaxSize() ,
                                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                error = painterResource(android.R.drawable.ic_dialog_alert)
                            )

                            // 4. A dark gradient overlay so the white button pops
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.2f))
                            )

                            // 5. The floating "Tap to view" button in the center
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.9f),
                                shadowElevation = 4.dp,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF5B75E6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tap for interactive map",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
        }
            val currentValid = issue?.votevalid ?: 0
            val currentInvalid = issue?.voteinvalid ?: 0
            // Calculate the math
            val totalVotes = currentValid + currentInvalid

            Spacer(modifier = Modifier.height(16.dp))


            // Prevent dividing by zero if nobody has voted yet!
            val accuracyRatio = if (totalVotes > 0) {
                currentValid.toFloat() / totalVotes.toFloat()
            } else {
                0.0f // Default to 50% if no votes exist
            }
            // Convert the decimal (e.g., 0.75) into a nice whole number (75)
            val accuracyPercentage = (accuracyRatio * 100).toInt()

            // --- 3. Validate Issue Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Validate Issue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CONFIDENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "$accuracyPercentage%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF388E3C) // Green
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Is this report accurate?", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))



                    // Simple Slider for visual representation
//                    Slider(
//                        value = accuracyRatio,
//                        onValueChange = {},
//                        enabled = false, // Read-only
//                        colors = SliderDefaults.colors(
//                            disabledThumbColor = Color.Transparent,
//                            disabledActiveTrackColor = Color(0xFF388E3C), // Green
//                            disabledInactiveTrackColor = Color(0xFFD32F2F) // Red
//                        )
//                    )
                    // 2. THE MAGIC: Tell Compose to smoothly animate to that target!
                    val animatedRatio by animateFloatAsState(
                        targetValue = accuracyRatio,
                        animationSpec = tween(
                            durationMillis = 800, // Takes 0.8 seconds to slide into place
                            easing = FastOutSlowInEasing // Starts fast, slows down smoothly at the end
                        ),
                        label = "AccuracyBarAnimation"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            // 1. This clips the entire container into a perfect pill shape
                            .clip(RoundedCornerShape(50))
                            // 2. light gray if invalid then red
                            .background(if (totalVotes > 0) Color(0xFFD32F2F) else Color.LightGray)
//                        )
                    )
                    {
                        // We always keep this box here so it can animate smoothly
                        Box(
                            modifier = Modifier
                                // Use the animated fraction to slide the width
                                .fillMaxWidth(fraction = animatedRatio)
                                .fillMaxHeight()
                                // FILL COLOR: Transparent if 0 votes. Green if >= 1 vote.
                                .background(if (totalVotes > 0) Color(0xFF388E3C) else Color.Transparent)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("INVALID", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("VALID", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        VoteButton(
                            modifier = Modifier.weight(1f),
                            text = "Valid Issue",
                            count = currentValid,
                            icon = Icons.Default.CheckCircle,
                            isSelected = myCurrentVote == "valid",
                            activeColor = Color(0xFF1A9058),       // Dark Green text/border
                            activeContainerColor = Color(0xFFD1E7DD), // Light Green background
                            onClick = {
//                                myCurrentVote = if (myCurrentVote == "valid") null else "valid"
//                                castVote(issue?.issueId ?: "none", myCurrentVote)

                                // 1. Instantly flip the UI state
                                myCurrentVote = if (myCurrentVote == "valid") null else "valid"

                                // 2. Cancel any running timer
                                debounceJob?.cancel()

                                // 3. Start a new 500ms countdown before writing to Firebase
                                debounceJob = coroutineScope.launch {
                                    delay(500)
                                    castVote(issue?.issueId ?: "none", oldVote = serverVote, newVote = myCurrentVote ,
                                        issue?.votevalid ?: 0, issue?.voteinvalid ?: 0
                                    )
                                }
                            })
                        VoteButton(
                            modifier = Modifier.weight(1f),
                            text = "Invalid Issue",
                            count = currentInvalid,
                            icon = Icons.Default.Cancel,
                            isSelected = myCurrentVote == "invalid",
                            activeColor = Color(0xB2EE3E3E),         // Dark Red text/border
                            activeContainerColor = Color(0xFFF8D7DA),   // Light Red background
                            onClick = {
//                                myCurrentVote = if (myCurrentVote == "invalid") null else "invalid"
//                                castVote(issue?.issueId ?: "none", myCurrentVote)

                                // 1. Instantly flip the UI state
                                myCurrentVote = if (myCurrentVote == "invalid") null else "invalid"

                                // 2. Cancel any running timer
                                debounceJob?.cancel()

                                // 3. Start a new 500ms countdown before writing to Firebase
                                debounceJob = coroutineScope.launch {
                                    delay(500)
                                    castVote(
                                        issue?.issueId ?: "none",
                                        oldVote = serverVote,
                                        newVote = myCurrentVote,
                                        issue?.votevalid ?: 0,
                                        issue?.voteinvalid ?: 0
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Recent Activity Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // User Comment
                    Row(modifier = Modifier.fillMaxWidth(),
                        // EQUIVALENT TO CrossAxisAlignment
                        Arrangement.Start , Alignment.Top) {
                        // Avatar Placeholder
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF0F0F0), CircleShape)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "John Doe",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "10m ago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "I drive by here every day, it's getting worse. The depth seems to have increased since the rain yesterday.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
    // --- SHOW THE MAP DIALOG ---
    if (showMapDialog) {
        val lat= issue?.latitude ?: 0.0;
        val lng = issue?.longitude ?: 0.0;

        val issueLocation = if (lat != 0.0 && lng != 0.0) {
            LatLng(lat, lng)
        } else {
            null // If invalid, it will safely fall back to Surat in the dialog
        }

        MapLocationPickerDialog(
            initialLocation = issueLocation,
            isReadOnly = true, // Hides the confirm button!
            onDismiss = { showMapDialog = false },
            onLocationConfirmed = { _, _, _ -> } // We leave this empty because there is no confirm button in Read-Only mode
        )
//        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(LocalContext.current) }

//        fetchCurrentLocation(
//            LocalContext.current, )
    }
}

// --- Helper Composables ---

@Composable
fun TagChip(text: String, backgroundColor: Color, textColor: Color) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VoteButton(
    modifier: Modifier = Modifier,
    text: String,
    count: Long,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    activeContainerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) activeContainerColor else Color.White,
            contentColor = if (isSelected) activeColor else Color.Gray
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) activeColor else Color.LightGray
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        )
        {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$text ($count)",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
fun castVote(
    issueId: String,
    oldVote: String?,
    newVote: String?,
    votevalid: Long,
    voteinvalid: Long
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance()

    // If they didn't actually change anything, don't waste a database call!
    if (oldVote == newVote) return

    // We use a Map to update multiple different locations in the database at the exact same time
    val updates = hashMapOf<String, Any?>()

    // --- 1. Update the User's Vote Record ---
    if (newVote == null) {
        updates["/issue_votes/$issueId/$userId"] = null // Remove them
    } else {
        updates["/issue_votes/$issueId/$userId"] = newVote // Save "valid" or "invalid"
    }

    // --- 2. Reverse the OLD Math ---
    // If they had a previous vote, we need to subtract it from the total
    if (oldVote == "valid" && votevalid > 0) {
        updates["/issues/$issueId/votevalid"] = ServerValue.increment(-1)
    } else if (oldVote == "invalid" && voteinvalid > 0) {
        updates["/issues/$issueId/voteinvalid"] = ServerValue.increment(-1)
    }

    // --- 3. Apply the NEW Math ---
    // Now we add their new vote to the correct total
    if (newVote == "valid") {
        updates["/issues/$issueId/votevalid"] = ServerValue.increment(1)
    } else if (newVote == "invalid") {
        updates["/issues/$issueId/voteinvalid"] = ServerValue.increment(1)
    }

    // --- 4. Execute everything atomically ---
    database.reference.updateChildren(updates)
}
@Preview(widthDp = 500 , heightDp = 1500)
@Composable
fun IssueDetailScreenPreview() {
    CivicConnectTheme {
        IssueDetailScreen( CivicIssue() , onBackClick = {})
    }
}