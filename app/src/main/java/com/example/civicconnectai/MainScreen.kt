package com.example.civicconnectai

import CivicIssue
import IssueViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.civicconnectai.bottomNavScreens.HomeScreen
import com.example.civicconnectai.bottomNavScreens.MapScreen
import com.example.civicconnectai.bottomNavScreens.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    reportIssueScreen: () -> Unit,
    onIssueClick: (String) -> Unit
// Pass this up from Home
) {
    // 1. This variable tracks which "Fragment" is visible
    var selectedTab by remember { mutableStateOf("Home") }
    val sharedViewModel: IssueViewModel = viewModel()

    // storing data from firebase
    var issuesList by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    // 2. Search State
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) } // Controls if search is expanded

    //  Bottom Sheet State
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    //  Filter Selections
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Newest") }



    //  Lists of your categories to map through
    val categories = listOf("All", "Roads", "Water", "Electricity", "Public Property")
    val statuses = listOf("All", "Open", "Pending", "Resolved")
    val sortOptions = listOf("Newest", "Most Voted", "Closest to Me")

    // 3. Filter Logic
    val filteredIssues = remember(searchQuery, selectedCategory, selectedStatus, sortBy, issuesList) {
        // A. Filter the list
        var currentList = issuesList.filter { issue ->

            // Check Search
            val matchesSearch = if (searchQuery.isBlank()) true else {
                issue.title?.contains(searchQuery, ignoreCase = true) == true ||
                        issue.description?.contains(searchQuery, ignoreCase = true) == true
            }

            // Check Category
            val matchesCategory = if (selectedCategory == "All") true else {
                issue.category == selectedCategory
            }

            // Check Status
            val matchesStatus = if (selectedStatus == "All") true else {
                issue.status == selectedStatus
            }

            // Keep the issue only if it matches ALL active filters
            matchesSearch && matchesCategory && matchesStatus
        }
        // B. Sort the list
        currentList = when (sortBy) {
            "Newest" -> currentList.sortedByDescending { it.timestamp } // Assuming you have a timestamp!
            "Most Voted" -> currentList.sortedByDescending { it.votevalid } // Assuming you track total votes
            // Note: "Closest to Me" requires actual GPS math, which we can add later!
            else -> currentList
        }

        // C. Return the final, polished list to the UI
        currentList
    }



    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                // Home Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == "Home",
                    onClick = { selectedTab = "Home" },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B75E6))
                )
                // Map Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                    label = { Text("Map") },
                    selected = selectedTab == "Map",
                    onClick = { selectedTab = "Map" },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B75E6))
                )
                // Profile Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == "Profile",
                    onClick = { selectedTab = "Profile" },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B75E6))
                )
            }
        }
    ) { paddingValues ->
        // 2. This is the "Fragment Container" - it swaps content automatically
        // We pass 'paddingValues' so the content doesn't get hidden behind the bottom bar
        Surface(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                "Home" -> HomeScreen(viewModel = sharedViewModel,reportIssueScreen = reportIssueScreen, onIssueClick = onIssueClick)
                "Map" -> MapScreen( viewModel = sharedViewModel, reportIssueScreen = reportIssueScreen, onIssueClick = onIssueClick) // You need to create this function
                "Profile" -> ProfileScreen() // You need to create this function
            }
        }
    }
}
