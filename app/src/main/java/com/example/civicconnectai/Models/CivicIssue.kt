data class CivicIssue(
    val issueId: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0, // contains latitude and longitude
    val address: String ="",
    val imageUrl: String = "", // This will hold the Supabase URL
    val status: String = "Pending", // Default status for a new report
    val timestamp: Long = 0L ,
    val votevalid: Long = 0L,
    val voteinvalid: Long = 0L
)