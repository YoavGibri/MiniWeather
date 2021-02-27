package com.yoavgibri.miniweather.network

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.yoavgibri.miniweather.Do
import com.yoavgibri.miniweather.models.OpenWeather
import com.yoavgibri.miniweather.network.OpenWeatherService.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkManager(var context: Context) {


    private val openWeatherService: OpenWeatherService = initOpenWeatherService()

    private fun initOpenWeatherService(): OpenWeatherService {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().apply {
                    addInterceptor(AppIDInterceptor())
                    //addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
                }.build())
                .build().create(OpenWeatherService::class.java)
    }


    fun getWeatherFromServer(weatherRequest: WeatherRequest, listener: OnResponse) {
        val openWeatherApi = openWeatherService

        weatherRequest.apply {
            openWeatherApi.getWeather(unitsFormat, currentLatString, currentLongString)
                    .enqueue(object : Callback<OpenWeather?> {
                        override fun onResponse(call: Call<OpenWeather?>, response: Response<OpenWeather?>) {
                            if (response.isSuccessful)
                                response.body()?.let { listener.onResponse(it) }
                            else
                                Do.logError(response.message(), context)
                        }

                        override fun onFailure(call: Call<OpenWeather?>, t: Throwable) {
                            Do.logError(t.message, context)
                        }
                    })
        }
    }


    public interface OnResponse {
        fun onResponse(weatherResponse: OpenWeather)
    }

}

