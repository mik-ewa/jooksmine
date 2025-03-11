package com.example.fitness_tracking_app.data

data class LanguageData(
    val language: String,
    val flag: String,
    val code: String,
    var isSelected: Boolean = false
)

data class LanguagesResponse(
     var languages: List<LanguageData>
)