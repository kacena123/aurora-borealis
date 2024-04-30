package com.example.aurora.Interfaces

import com.example.aurora.Models.DaylyForecast16.DailyForecast16
import com.example.aurora.Models.HourlyForecast4.HourlyForecast4
import com.example.aurora.Models.LocationItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("geo/1.0/reverse")
    fun getData(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("limit") limit:Int,
        @Query("appid") appid:String
    ): Call<List<LocationItem>>
    //?lat={lat}&lon={lon}&limit=1&appid=a06c0e568b038dfd7e97e1f445253141
    //@Path("lat") lat:String, @Path("lon") lon:String
    //@GET("posts/{id}")
    //fun getData(@Path("id") id:String): Call

    //hodinova predpoved
    @GET("/data/2.5/forecast/hourly")
    fun getHodinovaPredpoved(
        @Query("lat") lat:String,
        @Query("lon") lon: String,
        @Query("appid") appid:String
    ):Call<HourlyForecast4>

    @GET("/data/2.5/forecast/daily")
    fun getDlhodobaPredpoved(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("cnt") cnt:String,
        @Query("appid") appid: String
    ):Call<DailyForecast16>


}