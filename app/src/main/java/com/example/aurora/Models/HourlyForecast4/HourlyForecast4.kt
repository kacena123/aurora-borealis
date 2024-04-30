package com.example.aurora.Models.HourlyForecast4

data class HourlyForecast4(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Hour>,
    val message: Int
)
