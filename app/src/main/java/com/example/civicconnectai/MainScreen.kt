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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.civicconnectai.bottomNavScreens.HomeScreen
import com.example.civicconnectai.bottomNavScreens.MapScreen
import com.example.civicconnectai.bottomNavScreens.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    defaultfilter: String ,
    reportIssueScreen: () -> Unit,
    onIssueClick: (String) -> Unit ,
    onLogoutSuccess: () -> Unit
// Pass this up from Home
) {
    // 1. This variable tracks which "Fragment" is visible
    var selectedTab by remember { mutableStateOf("Home") }
    val sharedViewModel: IssueViewModel = viewModel()


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
                "Home" -> HomeScreen(viewModel = sharedViewModel, defaultfilter = defaultfilter , reportIssueScreen = reportIssueScreen, onIssueClick = onIssueClick)
                "Map" -> MapScreen( viewModel = sharedViewModel, reportIssueScreen = reportIssueScreen, onIssueClick = onIssueClick) // You need to create this function
                "Profile" -> ProfileScreen(navController = navController ,viewModel = sharedViewModel , onIssueClick = onIssueClick , onLogoutSuccess = onLogoutSuccess ) // You need to create this function
            }
        }
    }
}
