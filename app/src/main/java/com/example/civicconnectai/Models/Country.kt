package com.example.civicconnectai.Models

data class Country(
    val name: String,
    val dialCode: String,
    val flag: String
)
object CountryUtils {
    val globalCountries = listOf(
        Country("India", "+91", "🇮🇳"),
        Country("United States", "+1", "🇺🇸"),
        Country("United Kingdom", "+44", "🇬🇧"),
        Country("Canada", "+1", "🇨🇦"),
        Country("Australia", "+61", "🇦🇺"),
        Country("United Arab Emirates", "+971", "🇦🇪"),
        Country("Germany", "+49", "🇩🇪"),
        Country("France", "+33", "🇫🇷"),
        Country("Italy", "+39", "🇮🇹"),
        Country("Spain", "+34", "🇪🇸"),
        Country("Brazil", "+55", "🇧🇷"),
        Country("Mexico", "+52", "🇲🇽"),
        Country("Japan", "+81", "🇯🇵"),
        Country("South Korea", "+82", "🇰🇷"),
        Country("China", "+86", "🇨🇳"),
        Country("South Africa", "+27", "🇿🇦"),
        Country("Nigeria", "+234", "🇳🇬"),
        Country("Egypt", "+20", "🇪🇬"),
        Country("Saudi Arabia", "+966", "🇸🇦"),
        Country("Singapore", "+65", "🇸🇬"),
        Country("Malaysia", "+60", "🇲🇾"),
        Country("New Zealand", "+64", "🇳🇿"),
        Country("Argentina", "+54", "🇦🇷"),
        Country("Colombia", "+57", "🇨🇴"),
        Country("Russia", "+7", "🇷🇺"),
        Country("Turkey", "+90", "🇹🇷"),
        Country("Netherlands", "+31", "🇳🇱"),
        Country("Switzerland", "+41", "🇨🇭"),
        Country("Sweden", "+46", "🇸🇪"),
        Country("Pakistan", "+92", "🇵🇰"),
        Country("Bangladesh", "+880", "🇧🇩")
    )
}