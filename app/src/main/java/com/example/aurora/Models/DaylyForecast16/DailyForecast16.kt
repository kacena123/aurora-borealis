package com.example.aurora.Models.DaylyForecast16

data class DailyForecast16(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<day>,
    val message: Double
)
