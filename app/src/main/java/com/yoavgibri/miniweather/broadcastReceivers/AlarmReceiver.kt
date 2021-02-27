package com.yoavgibri.miniweather.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yoavgibri.miniweather.Do
import com.yoavgibri.miniweather.WeatherManager
import com.yoavgibri.miniweather.WeatherNotification
import com.yoavgibri.miniweather.models.OpenWeather

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Do.logToFile("AlarmReceiver - onReceive", context)

        WeatherManager(context).getCurrentWeather(object : WeatherManager.OnWeatherLoad {
            override fun onWeather(weather: OpenWeather) {
                Do.logToFile("AlarmReceiver - onWeather", context)

                val notification = WeatherNotification(context)
                notification.updateWeather(weather)

            }
        })
    }
}
