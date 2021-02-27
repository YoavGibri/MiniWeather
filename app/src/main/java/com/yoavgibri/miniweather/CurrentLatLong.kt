package com.yoavgibri.miniweather

/**
 * Created by Yoav on 18/11/17.
 */
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.provider.Settings
import android.util.Log


class CurrentLatLong {
    var currentLat: Double = 0.0
    var currentLong: Double = 0.0
    internal var mcontext: Activity? = null
    internal var status = 0

    fun currentLatLong(context: Activity): Int {

        mcontext = context

        if (isNetworkAvailable(context)) {
            navigation(context)

        } else {
            val alert = AlertDialog.Builder(mcontext)
            alert.setIcon(android.R.drawable.ic_dialog_alert)
            alert.setTitle("")
            alert.setMessage("Network_unAvailable")
            alert.setPositiveButton("Ok") { _, _ -> mcontext?.finish() }
            alert.show()
        }
        return status


    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
    }


    private fun navigation(context: Activity) {


        val builder = AlertDialog.Builder(mcontext)
        if (hasGpsOnDevice()) {
            if (isGpsEnabledOnDevice) {
                try {
                    val gpsTracker = GPSManager(context)
                    currentLat = gpsTracker.getLocation()?.latitude!!
                    currentLong = gpsTracker.getLocation()?.longitude!!

                    status = 1
                    Log.d("location status", status.toString() + "")
                } catch (e: Exception) {

                }

            } else {
                showCustomDialog()
            }
        } else {
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setTitle("")
            builder.setMessage("This device does not have GPS feature")
            builder.setPositiveButton("Ok", null)
            builder.show()
        }
    }

    private fun hasGpsOnDevice(): Boolean {
        val pm = mcontext?.packageManager
        val hasGps = pm!!.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        return hasGps
    }

    private val isGpsEnabledOnDevice: Boolean
        get() {
            val locManager = mcontext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(mcontext)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setTitle("")
        builder.setMessage("Please enable your device GPS.")
        builder.setCancelable(false)
        builder.setPositiveButton("Ok") { dialog, which ->
            mcontext?.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0)

        }
        builder.show()
    }

    private fun turnGPSOn() {

        val intent = Intent("android.location.GPS_ENABLED_CHANGE")
        intent.putExtra("enabled", true)
        mcontext?.sendBroadcast(intent)
    }
}