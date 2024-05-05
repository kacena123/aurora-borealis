package com.example.aurora.Models

import org.threeten.bp.LocalDateTime
import java.util.Date

data class SkodecModel(
    var id: String? = null,
    var userID: String? = null,
    var userName: String? = null,
    var nazovSkodca: String? = null,
    var sirka: String? = null,
    var dlzka: String? = null,
    var lokalita: String? = null,
    var popis: String? = null,
    //var date : LocalDateTime? = null
)
