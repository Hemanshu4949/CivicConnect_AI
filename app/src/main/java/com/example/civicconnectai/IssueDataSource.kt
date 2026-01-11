package com.example.civicconnectai// Create a 'data' package if you want

import com.example.civicconnectai.bottomNavScreens.IssueItem

// A simple singleton object to act as our "Database"
object IssueDataSource {
    val issues = listOf(
        IssueItem(
            "1", "Pothole on 5th Ave", "Westside Avenue", "Open", "2h ago",
            R.drawable.pothole
        ),
        IssueItem(
            "2", "Streetlight flickering", "Westside Avenue", "In Progress", "5h ago",
            R.drawable.accedent
        ),
        IssueItem(
            "3", "Graffiti on Park Wall", "Central Park North", "Resolved", "1d ago",
            R.drawable.img
        ),
        IssueItem(
            "4", "Missed Trash Collection", "Maple Street", "Open", "2d ago",
            R.drawable.garbage
        ),
        IssueItem(
            "5", "Broken Park Bench", "Central Park", "Open", "3d ago",
            R.drawable.garbage
        )
    )

    // Helper function to find an issue by ID
    fun getIssueById(id: String?): IssueItem? {
        return issues.find { it.id == id }
    }
}