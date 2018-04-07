package com.yoavgibri.miniweather.activities

import android.app.PendingIntent
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.broadcastReceivers.LocationUpdatesBroadcastReceiver
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.Manifest.permission.*
import android.app.Activity
import android.content.IntentSender
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.yoavgibri.miniweather.R
import com.yoavgibri.miniweather.models.OpenWeather
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val TAG = "MainActivity"
    private val REQUEST_CODE_SETTINGS = 5432
    private val REQUEST_CODE_CHECK_LOCATION_SETTINGS: Int = 433
    lateinit var notificationManager: WeatherNotification


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        versionTextView.text = "Ver " + BuildConfig.VERSION_NAME

        //askForPermissions()
        notificationManager = WeatherNotification(this)

        buttonSettings.setOnClickListener {
            startActivityForResult(Intent(this, SettingsActivity::class.java), REQUEST_CODE_SETTINGS)
        }

        buttonSettings.setOnLongClickListener {
            val devIntent = Intent(this, DevActivity::class.java)
            startActivity(devIntent)
            return@setOnLongClickListener true
        }

        registerToggle.setOnCheckedChangeListener({ buttonView, isChecked ->
            registerToggleOnCheck(isChecked)
        })

    }

    private fun registerToggleOnCheck(isChecked: Boolean) {
        if (isChecked) {
            if (mFusedLocationClient != null) {
                Do.setIsRegisteredForLocationUpdates(this, true)


                val locationSettingRequest = LocationSettingsRequest.Builder().addLocationRequest(LocationHelper.createLocationRequest()).build()
                val client = LocationServices.getSettingsClient(this)
                val task: Task<LocationSettingsResponse> = client.checkLocationSettings(locationSettingRequest)
                task.addOnSuccessListener { locationSettingsResponse ->
                    requestLastLocationAndUpdateWeather()
                    mFusedLocationClient?.requestLocationUpdates(LocationHelper.createLocationRequest(), getPendingIntent())
                }

                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            exception.startResolutionForResult(this@MainActivity, REQUEST_CODE_CHECK_LOCATION_SETTINGS)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
                Log.i(TAG, "MainActivity - registerSwitch OnClick - Checked")
            } else {
                if (askForPermissions()) {
                    registerToggleOnCheck(true)
                }
            }

        } else {
            Do.setIsRegisteredForLocationUpdates(this, false)
            if (mFusedLocationClient != null) mFusedLocationClient?.removeLocationUpdates(getPendingIntent())
            notificationManager.cancelNotification()
            Log.i(TAG, "MainActivity - registerSwitch OnClick - UnChecked")
        }
    }

    private fun requestLastLocationAndUpdateWeather() {
        if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener {

                if (it != null) {
                    //  save last location to sharedPreferences:
                    Do.saveLocationToSharedPreferences(this, it.latitude, it.longitude)
                }

                WeatherManager(this).getCurrentWeatherJson(object : WeatherManager.OnWeatherLoad {
                    override fun onWeather(weather: OpenWeather) {
                        notificationManager.updateWeather(weather)
                    }
                })
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Handler().postDelayed({ registerToggle.isChecked = Do.getIsRegisteredForLocationUpdates(this) }, 1000)
    }


    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        //intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun askForPermissions(): Boolean {
        val permissionsList = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE, INTERNET)

        return if (permissionsList.any { p -> checkSelfPermission(this, p) != PERMISSION_GRANTED }) {
            // need to request permissions
            ActivityCompat.requestPermissions(this, permissionsList, DevActivity.PERMISSIONS_REQUEST)
            false
        } else {
            // all permissions granted!
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            DevActivity.PERMISSIONS_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                    if (registerToggle.isChecked) registerToggleOnCheck(true)
                    else registerToggle.isChecked = true

                } else {
                    AlertDialog.Builder(this).setTitle("Permissions").setMessage("Hi there.\nWe need those permission in order to get weather updates, and to keep errors log.\n" +
                            "You won't be able to use this app unless you approve all of the permission.")
                            .setPositiveButton("go to permissions", { dialogInterface, i -> askForPermissions() })
                            .setOnDismissListener { registerToggle.isChecked = false }
                            .show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            requestLastLocationAndUpdateWeather()
        }

        if (requestCode == REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                requestLastLocationAndUpdateWeather()
            } else {
                val snackbar: Snackbar = Snackbar.make(findViewById(R.id.rootLayout), getString(R.string.turn_on_location_message), Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("TURN ON", { startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                snackbar.show()
                registerToggle.isChecked = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
