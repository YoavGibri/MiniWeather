package com.yoavgibri.miniweather.network

import com.yoavgibri.miniweather.models.OpenWeather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather")
    fun getWeather(@Query("units") units: String, @Query("lat") latitude: String, @Query("lon") longitude: String): Call<OpenWeather>
}