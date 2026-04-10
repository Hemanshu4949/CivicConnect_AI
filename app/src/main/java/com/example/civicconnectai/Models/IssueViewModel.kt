import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class IssueViewModel : ViewModel() {


// 1. Raw Data & Loading State
private val _masterIssuesList = MutableStateFlow<List<CivicIssue>>(emptyList())
    val issuelist : StateFlow<List<CivicIssue>> = _masterIssuesList.asStateFlow();

private val _isLoading = MutableStateFlow(true)
val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

// 2. Filter States
private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

private val _selectedCategory = MutableStateFlow("All")
val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

private val _selectedStatus = MutableStateFlow("All")
val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

private val _sortBy = MutableStateFlow("Newest")
val sortBy: StateFlow<String> = _sortBy.asStateFlow()

// 3. The Live Filtered List (Combines 5 variables instantly!)
val filteredIssues: StateFlow<List<CivicIssue>> = combine(
    _masterIssuesList,
    _searchQuery,
    _selectedCategory,
    _selectedStatus,
    _sortBy
) { list, query, category, status, sort ->

    // A. Filter
    var currentList = list.filter { issue ->
        val matchesSearch = if (query.isBlank()) true else {
            issue.title.contains(query, ignoreCase = true) || issue.description.contains(query, ignoreCase = true)
        }
        val matchesCategory = if (category == "All") true else {
            issue.category == category
        }
        val matchesStatus = if (status == "All") true else {
            issue.status == status
        }
        matchesSearch && matchesCategory && matchesStatus
    }

    // B. Sort
    currentList = when (sort) {
        "Newest" -> currentList.sortedByDescending { it.timestamp }
        "Most Voted" -> currentList.sortedByDescending { it.votevalid }
        else -> currentList
    }

    currentList // Return the final list to the UI

}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
)

init {
    fetchIssuesFromRealtimeDatabase()
}

// 4. Firebase Fetch Logic
private fun fetchIssuesFromRealtimeDatabase() {
    _isLoading.value = true
    val databaseRef = FirebaseDatabase.getInstance().getReference("issues")

    databaseRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tempIssues = mutableListOf<CivicIssue>()
            for (childSnapshot in snapshot.children) {
                val issue = childSnapshot.getValue(CivicIssue::class.java)
                if (issue != null) {
                    tempIssues.add(issue)
                }
            }
            // Update the master list. The 'combine' block will automatically
            // sort and filter this for us!
            _masterIssuesList.value = tempIssues
            _isLoading.value = false
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("IssueViewModel", "Failed to read value.", error.toException())
            _isLoading.value = false
        }
    })
}

// 5. Actions for the UI to call
fun updateSearchQuery(query: String) { _searchQuery.value = query }
fun updateCategory(category: String) { _selectedCategory.value = category }
fun updateStatus(status: String) { _selectedStatus.value = status }
fun updateSortBy(sort: String) { _sortBy.value = sort }
fun clearFilters() {
    _selectedCategory.value = "All"
    _selectedStatus.value = "All"
    _sortBy.value = "Newest"
}
}