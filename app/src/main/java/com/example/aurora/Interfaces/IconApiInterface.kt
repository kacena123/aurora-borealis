package com.example.aurora.Interfaces

import retrofit2.http.GET
import retrofit2.http.Path

interface IconApiInterface {

    @GET("img/wn/{id}")
    fun getIcon(
        @Path("id") id:String
    )
}