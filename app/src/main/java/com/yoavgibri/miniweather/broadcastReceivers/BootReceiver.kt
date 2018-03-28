package com.yoavgibri.miniweather.broadcastReceivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yoavgibri.miniweather.Do
import com.yoavgibri.miniweather.LocationHelper



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
            if(Do.getIsRegisteredForLocationUpdates(context)) {
                Do.logToFile("BootReceiver - OnReceive - Register State: REQUEST Location Updates.", context)
                val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
                mFusedLocationClient.requestLocationUpdates(LocationHelper.createLocationRequest(), Do.getPendingIntent(context))

                LocationHelper.setRequestingLocationUpdates(context, true)
                Do.logToFile("BootReceiver - OnReceive - Register State: Location updates requested.", context)
            } else {
                Do.logError("BootReceiver - OnReceive - Register State: DO NOT REQUEST Location Updates.", context)
            }
        }
    }


}