package com.yoavgibri.miniweather.activities

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.util.Log
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.broadcastReceivers.LocationUpdatesBroadcastReceiver
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Handler
import android.text.format.DateFormat
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.yoavgibri.miniweather.R
import com.yoavgibri.miniweather.databinding.ActivityMainBinding
import com.yoavgibri.miniweather.managers.SettingsManager
import com.yoavgibri.miniweather.models.OpenWeather
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    lateinit var notificationManager: WeatherNotification

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_CODE_SETTINGS = 5432
        const val REQUEST_CODE_CHECK_LOCATION_SETTINGS: Int = 433
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsManager.checkDefaultSettings()

        binding = ActivityMainBinding.inflate(layoutInflater).apply {

            setContentView(root)

            versionTextView.text = "Ver. ${BuildConfig.VERSION_NAME}"

            buttonSettings.setOnClickListener {
                startActivityForResult(Intent(this@MainActivity, SettingsActivity::class.java), REQUEST_CODE_SETTINGS)
            }

            buttonSettings.setOnLongClickListener {
                val devIntent = Intent(this@MainActivity, DevActivity::class.java)
                startActivity(devIntent)
                true
            }


            registerToggle.setOnCheckedChangeListener { _, isChecked -> registerToggleOnCheck(isChecked) }

        }

        notificationManager = WeatherNotification(this)
    }


    private fun registerToggleOnCheck(isChecked: Boolean) {
        val alarmHelper = AlarmManagerHelper(this)
        if (isChecked) {
            if (mFusedLocationClient != null) {
                Do.setIsRegisteredForLocationUpdates(this, true)

                val locationSettingResponse = LocationHelper.checkLocationSettings(this)

                locationSettingResponse.addOnSuccessListener {
                    requestLastLocationAndSetNotification()
                    Permissions.checkPermission(this@MainActivity, ACCESS_COARSE_LOCATION) {
                        mFusedLocationClient?.requestLocationUpdates(LocationHelper.createLocationRequest(), getPendingIntent())
                        alarmHelper.setRecurringAlarm()
                    }
                }

                locationSettingResponse.addOnFailureListener { exception ->
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
            alarmHelper.cancelAlarm()
            Log.i(TAG, "MainActivity - registerSwitch OnClick - UnChecked")
        }
    }

    private fun requestLastLocationAndSetNotification() {
        if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener {

                if (it != null) {
                    //  save last location to sharedPreferences:
                    Do.saveLocationToSharedPreferences(it.latitude, it.longitude)
                }
                WeatherManager(this).getCurrentWeather(object : WeatherManager.OnWeatherLoad {
                    override fun onWeather(weather: OpenWeather) {
                        notificationManager.updateWeather(weather)
                    }
                })
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Handler().postDelayed({ binding.registerToggle.isChecked = Do.getIsRegisteredForLocationUpdates(this) }, 1000)
    }


    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        //intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun askForPermissions(): Boolean {
        val permissionsList = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_NETWORK_STATE)

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

                    if (binding.registerToggle.isChecked) registerToggleOnCheck(true)
                    else binding.registerToggle.isChecked = true

                } else {
                    AlertDialog.Builder(this).setTitle("Permissions").setMessage("Hi there.\nWe need those permission in order to get weather updates, and to keep errors log.\n" +
                            "You won't be able to use this app unless you approve all of the permission.")
                            .setPositiveButton("go to permissions") { _, _ -> askForPermissions() }
                            .setOnDismissListener { binding.registerToggle.isChecked = false }
                            .show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            requestLastLocationAndSetNotification()
        }

        if (requestCode == REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                requestLastLocationAndSetNotification()
            } else {

                Snackbar.make(findViewById(R.id.rootLayout), getString(R.string.turn_on_location_message), Snackbar.LENGTH_INDEFINITE).apply {
                    setAction("TURN ON", View.OnClickListener { startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                    show()
                }

                binding.registerToggle.isChecked = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
