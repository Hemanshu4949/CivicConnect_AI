package com.example.civicconnectai.bottomNavScreens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.civicconnectai.IssueDataSource
import com.example.civicconnectai.ui.theme.CivicConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String?,
    onBackClick: () -> Unit
) {
    val issue = IssueDataSource.getIssueById(issueId)

    val backgroundColor = Color(0xFFF8F9FA)
    val infrastructureColor = Color(0xFFE3F2FD)
    val infrastructureTextColor = Color(0xFF1976D2)
    val highPriorityColor = Color(0xFFFFEBEE)
    val highPriorityTextColor = Color(0xFFD32F2F)

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

                 Image(
                     painter = painterResource(id = issue?.imageUrl ?: 0),
                     contentDescription = "Issue Image",
                     contentScale = ContentScale.FillBounds,
                     modifier = Modifier.fillMaxSize()
                 )
                if(issue?.imageUrl == null) {
                    Text(
                        "Image of Pothole",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
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
                        TagChip("INFRASTRUCTURE", infrastructureColor, infrastructureTextColor)
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
                            text = issue?.timeAgo ?: "none",
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
                                text = issue?.location ?: "none",
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

                    // Map Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)) // Gray map placeholder
                    ) {
                        Text(
                            "Map View Placeholder",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    }
                }
        }

            Spacer(modifier = Modifier.height(16.dp))

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
                                text = "82%",
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
                    Slider(
                        value = 0.82f,
                        onValueChange = {},
                        enabled = false, // Read-only
                        colors = SliderDefaults.colors(
                            disabledThumbColor = Color.Transparent,
                            disabledActiveTrackColor = Color(0xFF388E3C), // Green
                            disabledInactiveTrackColor = Color(0xFFD32F2F) // Red
                        )
                    )
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
                        ValidationButton(
                            text = "Valid Issue",
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF388E3C),
                            modifier = Modifier.weight(1f)
                        )
                        ValidationButton(
                            text = "Invalid Issue",
                            icon = Icons.Default.Cancel,
                            backgroundColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
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
fun ValidationButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { /* TODO */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(50.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Preview(widthDp = 500 , heightDp = 1500)
@Composable
fun IssueDetailScreenPreview() {
    CivicConnectTheme {
        IssueDetailScreen("2" , onBackClick = {})
    }
}