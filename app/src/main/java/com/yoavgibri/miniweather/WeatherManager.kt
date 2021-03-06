package com.yoavgibri.miniweather

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.yoavgibri.miniweather.models.OpenWeather

import org.json.JSONException

import java.net.URL

/**
 * Created by Yoav on 13/11/17.
 */

class WeatherManager(private var context: Context) {
    lateinit var listener: OnWeatherLoad


    //    fun getCurrentWeatherJson(currentLat: Double, currentLong: Double, listener: OnWeatherLoad) {
//        this.listener = listener
//        val sP = PreferenceManager.getDefaultSharedPreferences(context)
//        val unitsFormat = sP.getString(context.getString(R.string.sp_key_units_format), "metric")
//        val url = "http://api.openweathermap.org/data/2.5/weather?units=$unitsFormat&lat=$currentLat&lon=$currentLong&APPID=0234b7546d074c6839610dfd89210bee"
//        GetWeatherTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url)
//    }
    fun getCurrentWeatherJson(listener: OnWeatherLoad) {
        this.listener = listener
        val sP = PreferenceManager.getDefaultSharedPreferences(context)
        val unitsFormat = sP.getString(context.getString(R.string.sp_key_units_format), "metric")
        val currentLat = sP.getFloat(LocationHelper.KEY_LAST_KNOWN_LATITUDE, 0f).toDouble()
        val currentLong = sP.getFloat(LocationHelper.KEY_LAST_KNOWN_LONGITUDE, 0f).toDouble()

        val currentLatString = "%.4f".format(currentLat)
        val currentLongString = "%.4f".format(currentLong)

        val url = "http://api.openweathermap.org/data/2.5/weather?units=$unitsFormat&lat=$currentLatString&lon=$currentLongString&APPID=0234b7546d074c6839610dfd89210bee"
        GetWeatherTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url)
        Do.logToFile("WeatherManager - getCurrentWeatherJson, waiting for OnWeather - Lat,Long: $currentLatString,$currentLongString", context)
    }


    internal inner class GetWeatherTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String? {
            var jsonString : String = ""
            try {
            jsonString = URL(params[0]).readText()

            } catch (e: Exception){
                Do.logError(e.message, context)
            }
            return jsonString
        }

        override fun onPostExecute(jsonString: String) {
            try {

                val gson = Gson()
                val weather: OpenWeather = gson.fromJson(jsonString, OpenWeather::class.java)
                listener.onWeather(weather)


            } catch (e: JSONException) {
                e.printStackTrace()
                Do.logError(e.message, context)
            } catch (e: Exception) {
                Do.logError(e.message, context)
            }


        }
    }


    interface OnWeatherLoad {
        fun onWeather(weather: OpenWeather)
    }

}
