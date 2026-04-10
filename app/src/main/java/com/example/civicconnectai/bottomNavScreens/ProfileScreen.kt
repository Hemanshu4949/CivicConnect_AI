package com.example.civicconnectai.bottomNavScreens

import CivicIssue
import IssueViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController , viewModel : IssueViewModel , onIssueClick: (String) -> Unit , onLogoutSuccess: () -> Unit) {

    // --- BOTTOM SHEET STATE ---
    var showSettingsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    // 1. Get the current logged-in user ID
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // 2. Collect the global issue list from your ViewModel
    val globalIssuesList by viewModel.issuelist.collectAsState()

    // 3. Do all the math in a 'remember' block so it only recalculates when the list changes
    val profileStats = remember(globalIssuesList, currentUserId) {

        // A. Get ONLY this user's issues
        val myIssues = globalIssuesList.filter { it.userId == currentUserId }

        // B. Calculate Total Reports
        val reportsCount = myIssues.size

        // C. Calculate Total Upvotes
        // (Make sure "upvotes" perfectly matches the integer field in your CivicIssue data class.
        // If it's a list of users who voted, use .size instead)
        val totalUpvotes = myIssues.sumOf { it.votevalid }

        // D. Get the Latest 3 Reports
        // If your list is oldest-to-newest, .reversed() puts the newest first.
        val recentThree = myIssues.reversed().take(3)

        // Return a quick wrapper object with our results
        ProfileData(reportsCount, totalUpvotes, recentThree)
    }


    // --- MAIN LAYOUT: COLUMN ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Light Gray Background
             // Pushes content below system icons
    ) {

        // 1. CUSTOM HEADER (Replaces TopAppBar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Spacing for the title
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        // 2. SCROLLABLE CONTENT (Takes remaining space)
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Use weight to fill the rest of the screen
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // A. Profile Header Card
            item {
                ProfileHeaderCard(profileStats , onSettingsClick = { showSettingsSheet = true })
            }

            // B. Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Recent Reports",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                       TextButton(onClick = { // DIRECT LOGIC: Navigate to the home tab
                            navController.navigate("home?filter=my_reports") {
                                // Ensures we don't build up a massive backstack
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Text(
                                text = "View All",
                                color = Color(0xFF5B75E6),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                }
                if (profileStats.recentIssues.isEmpty()) {
                    Text(
                        text = "You haven't reported any issues yet.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                } else {
                    profileStats.recentIssues.forEach { issue ->
                        RecentReportItem(issue , onIssueClick)
                    }
                }
            }



            // Bottom Spacer
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
    // --- 2. THE BOTTOM SHEET ---
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // The Logout Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 1. Log the user out of Firebase
                            FirebaseAuth.getInstance().signOut()

                            // 2. Close the sheet
                            showSettingsSheet = false

                            // 3. Trigger the navigation back to your Login Screen
                            onLogoutSuccess()
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        // Use AutoMirrored so it flips correctly for right-to-left languages!
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFD32F2F) // A nice red color for a destructive action
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

// --- HELPER COMPOSABLES (Same as before) ---

@Composable
fun ProfileHeaderCard(profileStats : ProfileData ,onSettingsClick: () -> Unit ) {
    val user = FirebaseAuth.getInstance().currentUser

// Grab the raw Google Profile URL
    val rawPhotoUrl = user?.photoUrl?.toString()

// 🌟 PRO-TIP: Google returns a tiny, blurry 96x96 pixel image by default.
// We can trick Google into giving us a crisp, high-res image by changing the end of the URL!
    val highResProfileUrl = rawPhotoUrl?.replace("s96-c", "s400-c")


    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat design
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar + Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                val personIconPainter = rememberVectorPainter(image = Icons.Default.Person)
                // Placeholder Avatar
                AsyncImage(
                    // 1. Give it the high-res Google URL
                    model = highResProfileUrl, // Add a generic avatar PNG to your drawable folder just in case!
                    fallback = personIconPainter,
                    error = personIconPainter,
                    placeholder = personIconPainter,
                    contentDescription = "User Profile Picture",

                    // 2. Make it look like a professional avatar
                    modifier = Modifier
                        .size(100.dp) // Size of the profile picture
                        .clip(CircleShape) // Makes it perfectly round
                        .border(3.dp, Color(0xFF5B75E6), CircleShape), // Your app's Royal Blue border

                    contentScale = ContentScale.Crop // Ensures the image fills the circle without stretching
                )
                // Verification Badge
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF7B61FF), // Purple/Blue tint
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            user?.displayName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Role Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37), // Gold
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "COMMUNITY GUARDIAN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStat(count = profileStats.totalReports.toString(), label = "Issues\nReported")
                VerticalDivider()
                ProfileStat(count = profileStats.totalUpvotes.toString(), label = "Upvotes\nReceived")
                VerticalDivider()
                ProfileStat(count = "90%", label = "Impact Score", color = Color(0xFF7B61FF))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions Row
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { /* Edit Action */ },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7B61FF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7B61FF))
                ) {
                    Text("Edit Profile", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Settings Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onSettingsClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color(0xFFEEEEEE))
    )
}

@Composable
fun ProfileStat(count: String, label: String, color: Color = Color(0xFF7B61FF)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        )
    }
}

@Composable
fun RecentReportItem(issue:CivicIssue  , onIssueClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (issue.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(issue.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Issue Image",
                    contentScale = ContentScale.FillBounds, // Crops it perfectly to fit the box
                    modifier = Modifier
                        .size(94.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
            } else {
                // Placeholder if no image was uploaded
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .background(Color.LightGray)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                }
            }


            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    StatusChipProfile(issue.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Reported on ${getTimeAgo(issue.timestamp)} • ${issue.votevalid} Upvotes",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {onIssueClick(issue.issueId) })
                    {
                        Text(
                            text = "View details",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF7B61FF),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF7B61FF),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChipProfile(status: String) {
    val (bgColor, textColor) = when (status) {
        "Resolved" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        "Pending" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        "In Review" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        else -> Color.Gray to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
data class ProfileData(
    val totalReports: Int,
    val totalUpvotes: Long,
    val recentIssues: List<CivicIssue> // Use your actual data class name here
)