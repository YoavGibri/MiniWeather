package com.yoavgibri.miniweather

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import com.google.android.gms.location.LocationRequest
import java.text.DateFormat
import java.util.*

/**
 * Created by Yoav on 09/12/17.
 */

public class LocationHelper {

    companion object {

        private val KEY_LOCATION_UPDATES_REQUESTED: String? = "location-updates-requested"
        val KEY_LAST_KNOWN_LATITUDE: String? = "last_known_latitude"
        val KEY_LAST_KNOWN_LONGITUDE: String? = "last_known_longitude"

        fun setRequestingLocationUpdates(context: Context, requestUpdates: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, requestUpdates)
                    .apply()
        }

        fun getRequestingLocationUpdates(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false)
        }

        fun getLocationResultTitle(context: Context, locations: List<Location>): String {
            val numLocationsReported = context.resources.getQuantityString(R.plurals.num_locations_reported, locations.size, locations.size)
            return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(Date())
        }


        private val KEY_LOCATION_UPDATES_RESULT: String? = "location-update-result"
        fun setLocationUpdatesResult(context: Context, locations: List<Location> ) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations) + "\n" + getLocationResultText(context, locations)).apply()
        }

        fun getLocationUpdatesResult(context: Context): String {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(KEY_LOCATION_UPDATES_RESULT, "")
        }

        private fun getLocationResultText(context: Context, locations: List<Location>): String {
            if (locations.isEmpty()) {
                return context.getString(R.string.unknown_location)
            }
            val sb = StringBuilder()
            for (location in locations) {
                sb.append("(")
                sb.append(location.latitude)
                sb.append(", ")
                sb.append(location.longitude)
                sb.append(")")
                sb.append("\n")
            }
            return sb.toString()
        }

        val second : Long = 1000
        val minute : Long = 60 * second
        val hour : Long = 60 * minute

        private val FASTEST_UPDATE_INTERVAL: Long =  30 * minute
        private val UPDATE_INTERVAL: Long = hour
        private val MAX_WAIT_TIME: Long = 2 * hour

        fun createLocationRequest() : LocationRequest {
            return LocationRequest().apply {
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                // Sets the desired interval for active location updates. This interval is
                // inexact. You may not receive updates at all if no location sources are available, or
                // you may receive them slower than requested. You may also receive updates faster than
                // requested if other applications are requesting location at a faster interval.
                // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
                // less frequently than this interval when the app is no longer in the foreground.
                interval = UPDATE_INTERVAL
                // Sets the fastest rate for active location updates. This interval is exact, and your
                // application will never receive updates faster than this value.
                fastestInterval = FASTEST_UPDATE_INTERVAL
                // Sets the maximum time when batched location updates are delivered. Updates may be
                // delivered sooner than this interval.
                maxWaitTime = MAX_WAIT_TIME
            }
        }



    }
}