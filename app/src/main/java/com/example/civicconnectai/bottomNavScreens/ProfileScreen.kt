package com.example.civicconnectai.bottomNavScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.civicconnectai.R
import com.example.civicconnectai.ui.theme.CivicConnectTheme

// Data model for list items
data class RecentReport(
    val id: String,
    val title: String,
    val date: String,
    val upvotes: Int,
    val status: String,
    val imageRes: Int
)

@Composable
fun ProfileScreen() {
    // Sample Data
    val recentReports = listOf(
        RecentReport("1", "Pothole on Main St", "Oct 12", 12, "Resolved", R.drawable.pothole),
        RecentReport("2", "Broken Streetlight", "Nov 03", 5, "Pending", R.drawable.accedent),
        RecentReport("3", "Graffiti in Central Park", "Nov 05", 8, "In Review", R.drawable.garbage)
    )

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
                ProfileHeaderCard()
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
                    TextButton(onClick = { /* View All Logic */ }) {
                        Text(
                            text = "View All",
                            color = Color(0xFF5B75E6),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // C. List of Reports
            items(recentReports) { report ->
                RecentReportItem(report)
            }

            // Bottom Spacer
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// --- HELPER COMPOSABLES (Same as before) ---

@Composable
fun ProfileHeaderCard() {
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
                // Placeholder Avatar
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with real image
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFCC80))
                        .border(4.dp, Color(0xFFF0F0F0), CircleShape)
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
            Text(
                text = "Alex Johnson",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

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
                ProfileStat(count = "14", label = "Issues\nReported")
                VerticalDivider()
                ProfileStat(count = "82", label = "Upvotes\nReceived")
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
                        .clickable { /* Settings Action */ }
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
fun RecentReportItem(report: RecentReport) {
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
            Image(
                painter = painterResource(id = report.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    StatusChipProfile(report.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Reported on ${report.date} • ${report.upvotes} Upvotes",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View details",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF7B61FF),
                            fontWeight = FontWeight.Medium
                        )
                    )
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

@Preview
@Composable
fun ProfileScreenPreview() {
    CivicConnectTheme {
        ProfileScreen()
    }
}