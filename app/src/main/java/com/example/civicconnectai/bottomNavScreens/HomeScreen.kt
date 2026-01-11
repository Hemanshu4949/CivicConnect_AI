package com.example.civicconnectai.bottomNavScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.civicconnectai.IssueDataSource
import com.example.civicconnectai.bottomNavScreens.ReportIssueScreen
import com.example.civicconnectai.ui.theme.CivicConnectTheme

// --- Data Model for the UI ---
data class IssueItem(
    val id: String,
    val title: String,
    val location: String,
    val status: String, // "Open", "In Progress", "Resolved"
    val timeAgo: String,
    val imageUrl: Int = 0 // Placeholder for now
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    reportIssueScreen: () -> Unit,
    onIssueClick: (String) -> Unit
) {
    // Sample Data to match your image
    val allIssues = remember {
        IssueDataSource.issues
    }
    // 2. Search State
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) } // Controls if search is expanded

    // 3. Filter Logic
    val filteredIssues = if (searchQuery.isBlank()) {
        allIssues
    } else {
        allIssues.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }
    }

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
                            onQueryChange = { searchQuery = it },
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
                                                searchQuery = ""
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
                                supportingContent = { Text(issue.location) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .clickable {
                                        searchQuery = issue.title
                                        active = false
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
                        onClick = { /* Handle Sort/Filter Click */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune, // The Filter Icon
                                contentDescription = "Filter",
                                tint = Color.Black
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
                if (!active) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredIssues) { issue ->
                            IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun IssueCard(
    issue: IssueItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
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
            Image(
                painter = painterResource(id = issue.imageUrl), // Use the Int ID
                contentDescription = "Issue Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = issue.title,
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
                        text = issue.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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
                        text = issue.timeAgo,
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
        "Open" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F) // Pink/Red
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

@Preview
@Composable
fun HomeScreenPreview() {
    CivicConnectTheme {
        HomeScreen({}, {})
    }
}