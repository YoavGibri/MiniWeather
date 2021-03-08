package com.yoavgibri.miniweather

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.yoavgibri.miniweather.activities.DevActivity
import timber.log.Timber

/**
 * Created by Yoav on 10/12/17.
 */
class GPSManager(val mContext: Context) : LocationListener {
private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var canGetLocation = false
    private var location: Location? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()


    private var locationManager: LocationManager? = null


    fun getLocation(): Location? {
        if (mContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            try {
                locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

                println("******isGPSEnabled********" + isGPSEnabled)

                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                println("******isNetworkEnabled********" + isNetworkEnabled)

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager!!.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                            Log.d("GPS", "GPS Enabled")
                            if (locationManager != null) {
                                location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                Log.d("location", location!!.toString() + "")
                                if (location != null) {
                                    latitude = location!!.latitude
                                    longitude = location!!.longitude
                                } else if (isNetworkEnabled) {
                                    locationManager!!.requestLocationUpdates(
                                            LocationManager.NETWORK_PROVIDER,
                                            MIN_TIME_BW_UPDATES,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                                    Log.d("Network", "Network Enabled")
                                    if (locationManager != null) {
                                        location = locationManager!!
                                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                        Log.d("location", location!!.toString() + "")
                                        if (location != null) {
                                            latitude = location!!.latitude
                                            longitude = location!!.longitude
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isNetworkEnabled) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        Log.d("Network", "Network Enabled")
                        if (locationManager != null) {
                            location = locationManager!!
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            Log.d("location", location!!.toString() + "")
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude

                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            (mContext as AppCompatActivity).requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), DevActivity.PERMISSIONS_REQUEST)
        }

        return location

    }

    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSManager)
        }
    }

    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }

        return latitude
    }

    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }

        return longitude
    }

    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    override fun onLocationChanged(arg0: Location) {
        // TODO Auto-generated method stub

    }

    override fun onProviderDisabled(arg0: String) {
        // TODO Auto-generated method stub

    }

    override fun onProviderEnabled(arg0: String) {
        // TODO Auto-generated method stub

    }

    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle) {
        // TODO Auto-generated method stub

    }

    companion object {

        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private val MIN_TIME_BW_UPDATES = (1000 * 10 * 1).toLong() // 10 seconds
    }
}