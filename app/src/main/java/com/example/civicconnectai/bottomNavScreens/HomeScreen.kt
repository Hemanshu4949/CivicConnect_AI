package com.example.civicconnectai.bottomNavScreens

import CivicIssue
import IssueViewModel
import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: IssueViewModel,
    defaultfilter : String,
    reportIssueScreen: () -> Unit,
    onIssueClick: (String) -> Unit
) {

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Log.d("FCM", "Notification permission granted!")
            } else {
                Log.d("FCM", "Notification permission denied.")
            }
        }
    )

    // 2. Run this once when the Home Screen loads
    LaunchedEffect(Unit) {
        // Ask for permission if they are on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 3. Grab the Device Token and save it to your Database!
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val fcmToken = task.result
            Log.e("FCM", "My Device Token is: $fcmToken")

//            android.widget.Toast.makeText(LocalContext.current, "Token Generated!", android.widget.Toast.LENGTH_SHORT).show()

            // Save it to Firebase Realtime Database under their user profile
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.uid)
                    .child("fcmToken") // We save it in a new field called fcmToken
                    .setValue(fcmToken)
            }
        }
    }

// 1. Collect Data States from ViewModel
    val filteredIssues by viewModel.filteredIssues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    // view all filter
    val displayedIssues = remember(filteredIssues, defaultfilter) {
        if (defaultfilter == "my_reports") {
            // Filter by the current user's ID
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            filteredIssues.filter { it.userId == currentUserId }
        } else {
            filteredIssues
        }
    }


    // storing data from firebase
    var issuesList by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }


    // 2. Search State
    var active by rememberSaveable { mutableStateOf(false) } // Controls if search is expanded

    //  Bottom Sheet State
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterflag by remember {mutableStateOf(false)}

    if (defaultfilter != "my_reports" || filterflag)
    {
        issuesList = filteredIssues
    }
    else
    {
        issuesList = displayedIssues
    }


    //  Lists of your categories to map through
    val categories = listOf("All", "Pothole", "Streetlight", "Graffiti", "Trash")
    val statuses = listOf("All", "Open", "Pending", "Resolved")
    val sortOptions = listOf("Newest", "Most Voted", "Closest to Me")

//    // 3. Filter Logic
//    val filteredIssues = remember(searchQuery, selectedCategory, selectedStatus, sortBy, issuesList) {
//        // A. Filter the list
//        var currentList = issuesList.filter { issue ->
//
//            // Check Search
//            val matchesSearch = if (searchQuery.isBlank()) true else {
//                issue.title?.contains(searchQuery, ignoreCase = true) == true ||
//                        issue.description?.contains(searchQuery, ignoreCase = true) == true
//            }
//
//            // Check Category
//            val matchesCategory = if (selectedCategory == "All") true else {
//                issue.category == selectedCategory
//            }
//
//            // Check Status
//            val matchesStatus = if (selectedStatus == "All") true else {
//                issue.status == selectedStatus
//            }
//
//            // Keep the issue only if it matches ALL active filters
//            matchesSearch && matchesCategory && matchesStatus
//        }
//        // B. Sort the list
//        currentList = when (sortBy) {
//            "Newest" -> currentList.sortedByDescending { it.timestamp } // Assuming you have a timestamp!
//            "Most Voted" -> currentList.sortedByDescending { it.votevalid } // Assuming you track total votes
//            // Note: "Closest to Me" requires actual GPS math, which we can add later!
//            else -> currentList
//        }
//
//        // C. Return the final, polished list to the UI
//        currentList
//    }
//
//
//
//    LaunchedEffect(Unit) {
//        val databaseRef = FirebaseDatabase.getInstance().getReference("issues")
//
//        // addValueEventListener listens for real-time updates!
//        databaseRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val tempIssues = mutableListOf<CivicIssue>()
//                for (childSnapshot in snapshot.children) {
//                    val issue = childSnapshot.getValue(CivicIssue::class.java)
//                    if (issue != null) {
//                        tempIssues.add(issue)
//                    }
//                }
//                // Sort the list so the newest issues are at the top
//                issuesList = tempIssues.sortedByDescending { it.timestamp }
//                isLoading = false
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("FirebaseFetch", "Failed to read value.", error.toException())
//                isLoading = false
//            }
//        })
//    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Light Gray Background
        topBar = {
            // Header Row (Logo + Notification) - Hide when search is active
            if (!active) {
                // Custom Top Bar to match design
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CivicConnect",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                    IconButton(onClick = { /* Handle Notifications */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Black
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { reportIssueScreen() },
                containerColor = Color(0xFF5B75E6), // Royal Blue
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Report Issue",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = if (active) 0.dp else 24.dp)
        ) {

            // --- SEARCH BAR + SORT BUTTON ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The Search Bar
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onSearch = { active = false },
                            expanded = active,
                            onExpandedChange = { active = it },
                            placeholder = { Text("Search issues...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (active) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        modifier = Modifier.clickable {
                                            if (searchQuery.isNotEmpty()) {
                                                viewModel.updateSearchQuery("")
                                            } else {
                                                active = false
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    },
                    expanded = active,
                    onExpandedChange = { active = it },
                    windowInsets = WindowInsets(0.dp),
                    modifier = Modifier.weight(1f) // Takes available space
                ) {
                    // Suggestions List (Inside Search Bar)
                    LazyColumn {
                        items(filteredIssues) { issue ->
                            ListItem(
                                headlineContent = { Text(issue.title) },
                                supportingContent = { Text(issue.address) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .clickable {
                                         viewModel.updateSearchQuery(issue.category)
                                        active = false
                                        onIssueClick(issue.issueId)
                                    }
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // The Separate Sort/Filter Button (Hidden when searching)
                if (!active) {
                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier.size(56.dp), // Height matches standard SearchBar
                        shape = RoundedCornerShape(20.dp), // Fully rounded to match
                        color = Color.White,
                        shadowElevation = 4.dp, // Matches SearchBar elevation
                        onClick = { showFilterSheet = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val isFilterActive = selectedCategory != "All" || selectedStatus != "All"
                            Icon(
                                imageVector = Icons.Default.Tune, // The Filter Icon
                                contentDescription = "Filter",
                                tint = if (isFilterActive) Color(0xFF5B75E6) else Color.Black
                            )
                        }
                    }
                }
            }
//
//            // --- Search Bar Row (older search)---
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Search Field
//                Surface(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(50.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    color = Color.White,
//                    shadowElevation = 2.dp
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Search,
//                            contentDescription = null,
//                            tint = Color.Gray
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Search issues...",
//                            color = Color.Gray,
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }

//
//            // --- Issues List  normal one ---
//            LazyColumn(
//                contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                items(issues) { issue ->
//                    IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
//                }
//            }
//        }
            // --- MAIN LIST (When search is NOT expanded) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
//                if (!active) {
//                    LazyColumn(
//                        contentPadding = PaddingValues(bottom = 80.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        items(filteredIssues) { issue ->
//                            IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
//                        }
//                    }
//                }
//            }


            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (issuesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No issues reported yet. Be the first!", color = Color.Gray)
                }
            } else {
                // LazyColumn is the Jetpack Compose version of RecyclerView
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // This loops through every issue in your database
                    items(issuesList) { issue ->
                        IssueCard(issue = issue, onClick = { onIssueClick(issue.issueId) })
                    }
                }
            }
        }

    }

    }
    // --- THE FILTER BOTTOM SHEET ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(
                        start = 16.dp, bottom = 32.dp
                    )
            ) {
                Text("Filter Issues", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Category Row
                Text("Category", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.updateCategory( category) },
                            label = { Text(category) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Status Row
                Text("Status", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statuses) { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { viewModel.updateStatus(status) },
                            label = { Text(status) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Sort By Row
                Text("Sort By", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sortOptions) { option ->
                        FilterChip(
                            selected = sortBy == option,
                            onClick = { viewModel.updateSortBy(option) },
                            label = { Text(option) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // 4. Apply / Clear Buttons
                Row(modifier = Modifier.fillMaxWidth().padding(end = 15.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            // Reset everything
                            viewModel.updateCategory("All")
                            viewModel.updateStatus("All")
                            viewModel.updateSortBy("Newest")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All")
                    }

//                    Button(
//                        onClick = { showFilterSheet = false
//                            filterflag = true},
//                        modifier = Modifier.weight(1f),
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B75E6))
//                    ) {
//                        Text("Apply Filters")
//                    }
                }
            }
        }
    }
// --- END BOTTOM SHEET ---

}



@Composable
fun IssueCard(
    issue: CivicIssue,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail Image
            if (issue.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(issue.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Issue Image",
                    contentScale = ContentScale.FillBounds, // Crops it perfectly to fit the box
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
            } else {
                // Placeholder if no image was uploaded
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image Available", color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = issue.category,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = issue.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2 ,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Row: Status & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = issue.status)

                    Text(
                        text = getTimeAgo(issue.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status) {
        "Pending" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F) // Pink/Red
        "In Progress" -> Color(0xFFFFF3E0) to Color(0xFFF57C00) // Orange/Yellow
        "Resolved" -> Color(0xFFE8F5E9) to Color(0xFF388E3C) // Green
        else -> Color.Gray to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
fun getTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    var diff = currentTime - timestamp

    // Fallback just in case the device's clock is slightly out of sync
    if (diff < 0) {
        diff = 0
    }

    // Define time blocks in milliseconds
    val second = 1000L
    val minute = 60 * second
    val hour = 60 * minute
    val day = 24 * hour
    val year = 365 * day

    return when {
        diff >= year -> {
            val years = diff / year
            if (years == 1L) "1 year ago" else "$years years ago"
        }
        diff >= day -> {
            val days = diff / day
            if (days == 1L) "1 day ago" else "$days days ago"
        }
        diff >= hour -> {
            val hours = diff / hour
            if (hours == 1L) "1 hour ago" else "$hours hours ago"
        }
        diff >= minute -> {
            val minutes = diff / minute
            if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
        }
        else -> "Just now" // For anything less than a minute
    }
}