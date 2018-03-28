package com.yoavgibri.miniweather.broadcastReceivers

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.models.OpenWeather

/**
 * Created by Yoav on 27/03/18.
 */
class RefreshButtonReceiver : BroadcastReceiver() {
    val TAG: String = "RefreshButtonReceiver"


    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            Log.d(TAG, "=========== ONRECEIVE ============")
            doOnReceive(context!!)
        } catch (e: Exception) {
            Do.logError(e.message, context!!)
        }
    }

    companion object {

        const val ACTION_PROCESS_UPDATES: String = BuildConfig.APPLICATION_ID + "action.PROCESS.UPDATES"

        @SuppressLint("MissingPermission")
        fun doOnReceive(context: Context) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient?.lastLocation?.addOnSuccessListener {
                if (it != null) {
                    WeatherManager(context).getCurrentWeatherJson(it.latitude, it.longitude, object : WeatherManager.OnWeatherLoad {
                        override fun onWeather(weather: OpenWeather) {
                            val notificationManager = WeatherNotification(context)
                            notificationManager.updateWeather(weather)
                        }
                    })
                }
            }
//            if(Do.getIsRegisteredForLocationUpdates(context)) {
//                Do.logToFile("RefreshButtonReceiver - OnReceive - Register State: REQUEST Location Updates.")
//                val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//                mFusedLocationClient.requestLocationUpdates(LocationHelper.createLocationRequest(), Do.getPendingIntent(context))
//
//                LocationHelper.setRequestingLocationUpdates(context, true)
//                Do.logToFile("RefreshButtonReceiver - OnReceive - Register State: Location updates requested.")
//            } else {
//                Do.logError("RefreshButtonReceiver - OnReceive - Register State: DO NOT REQUEST Location Updates.", context)
//            }
        }

    }


}