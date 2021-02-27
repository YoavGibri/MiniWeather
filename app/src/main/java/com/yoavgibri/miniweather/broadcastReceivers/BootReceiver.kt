package com.yoavgibri.miniweather.broadcastReceivers

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.models.OpenWeather


/**
 * Created by Yoav on 25/11/17.
 */


class BootReceiver : BroadcastReceiver() {
    val TAG: String = "BootReceiver"


    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            Log.d(TAG, "=========== ONRECEIVE ============")
            if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
                Log.d(TAG, "=========== ${intent.action} ===========")
                doOnReceive(context!!)
            }

        } catch (e: Exception) {
            Do.logError(e.message, context!!)
        }
    }

    companion object {
        @SuppressLint("MissingPermission")
        fun doOnReceive(context: Context) {
            if (Do.getIsRegisteredForLocationUpdates(context)) {
                Do.logToFile("BootReceiver - OnReceive - Register State: REQUEST Location Updates.", context)

                //  Get fusedLocationProvider:
                val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
                if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    //  Save Last Location:
                    mFusedLocationClient.lastLocation?.addOnSuccessListener {
                        if (it != null) {
                            Do.saveLocationToSharedPreferences(it.latitude, it.longitude)
                        }
                    }

                    //  Register to location updates:
                    mFusedLocationClient.requestLocationUpdates(LocationHelper.createLocationRequest(), Do.getPendingIntent(context))

                    //  Get Weather and update notification:
                    WeatherManager(context).getCurrentWeather(object : WeatherManager.OnWeatherLoad {
                        override fun onWeather(weather: OpenWeather) {
                            WeatherNotification(context).updateWeather(weather)
                        }
                    })
                }

//                //  Register to notification updates by AlarmManager:
                AlarmManagerHelper(context).setRecurringAlarm()

                Do.logToFile("BootReceiver - OnReceive - Register State: Location updates requested.", context)
            } else {
                Do.logError("BootReceiver - OnReceive - Register State: DO NOT REQUEST Location Updates.", context)
            }
        }
    }


}