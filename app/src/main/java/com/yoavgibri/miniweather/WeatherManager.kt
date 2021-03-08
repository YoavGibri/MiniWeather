package com.yoavgibri.miniweather

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.yoavgibri.miniweather.managers.SettingsManager
import com.yoavgibri.miniweather.models.OpenWeather
import com.yoavgibri.miniweather.network.NetworkManager
import com.yoavgibri.miniweather.network.WeatherRequest

import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.net.URL

/**
 * Created by Yoav on 13/11/17.
 */

class WeatherManager(private var context: Context) {
    lateinit var listener: OnWeatherLoad


    fun getCurrentWeather(listener: OnWeatherLoad) {
        this.listener = listener

        val currentLat = SP.getFloat(LocationHelper.KEY_LAST_KNOWN_LATITUDE, 0f).toDouble()
        val currentLong = SP.getFloat(LocationHelper.KEY_LAST_KNOWN_LONGITUDE, 0f).toDouble()
        val unitsFormat = SettingsManager.getUnitFormat()

        val currentLatString = "%.4f".format(currentLat)
        val currentLongString = "%.4f".format(currentLong)

        val weatherRequest = WeatherRequest(unitsFormat, currentLatString, currentLongString)

        NetworkManager(context).getWeatherFromServer(weatherRequest, object : NetworkManager.OnResponse {
            override fun onResponse(weatherResponse: OpenWeather) {
                listener.onWeather(weatherResponse)
            }
        })

        Do.logToFile("WeatherManager - getCurrentWeatherJson, waiting for OnWeather - Lat,Long: $currentLatString,$currentLongString", context)
    }

//    private fun getWeatherFromServer(unitsFormat: String, currentLatString: String, currentLongString: String) {
//        val openWeatherApi = NetworkManager.openWeatherService
//
//        openWeatherApi.getWeather(unitsFormat, currentLatString, currentLongString)
//                .enqueue(object : Callback<OpenWeather?> {
//                    override fun onResponse(call: Call<OpenWeather?>, response: Response<OpenWeather?>) {
//                        if (response.isSuccessful)
//                            response.body()?.let { listener.onWeather(it) }
//                        else
//                            Do.logError(response.message(), context)
//                    }
//
//                    override fun onFailure(call: Call<OpenWeather?>, t: Throwable) {
//                        Do.logError(t.message, context)
//
//                    }
//                })
//
//    }


    interface OnWeatherLoad {
        fun onWeather(weather: OpenWeather)
    }

}
