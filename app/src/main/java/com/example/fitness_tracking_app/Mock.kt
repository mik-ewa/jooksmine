package com.example.fitness_tracking_app


import com.example.fitness_tracking_app.data.ProgressItemData
import com.example.fitness_tracking_app.features.home.HomeRecyclerItem
import com.github.mikephil.charting.data.Entry

val progressMock = listOf(
    ProgressItemData("Total distance in km", 22.0f),
    ProgressItemData("Hours spent running", 1.2f),
    ProgressItemData("Average pace", 7.3f),
    ProgressItemData("Calories burnt", 840.0f),
    ProgressItemData("Active days", 4.0f)
)

val statisticsYearData = listOf(
    Entry(0f, 13f), Entry(1f, 7f), Entry(2f, 0f), Entry(3f, 89f),
    Entry(4f, 22f), Entry(5f, 19f), Entry(6f, 0f), Entry(7f, 7f),
    Entry(8f, 2f), Entry(9f, 8f), Entry(10f, 12f), Entry(11f, 7f)
)

val statisticsWeekData = listOf(
    Entry(0f, 0f), Entry(1f, 6f), Entry(2f, 0f), Entry(3f, 0f),
    Entry(4f, 12f), Entry(5f, 6f), Entry(6f, 0f)
)

val monthsData = arrayOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec"
)

val weekData = arrayOf(
    "Mon",
    "Tue",
    "Wed",
    "Thur",
    "Fri",
    "Sat",
    "Sun"
)

val weatherMockInfo = HomeRecyclerItem.WeatherItem(
    feelsLike = 32.0,
    humidity = 20,
    temp = 29.7,
    description = "cloudy",
    main = "Clouds",
    date = "02/03/2025 22:08",
    wind = 43.4,
    city = "Mock City"
)

val autoMessage =
    "Hi there! Feel free to explore the whole app and check the functionalities. We are already friends, but you can search for more using dedicated fragment for it (I heard that we have a user named Mariolis or Karol...). Also feel free to go on a quick run and check how many kilometers you can make "
