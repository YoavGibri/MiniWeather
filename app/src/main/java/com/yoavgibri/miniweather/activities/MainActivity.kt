package com.yoavgibri.miniweather.activities

import android.app.PendingIntent
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.broadcastReceivers.LocationUpdatesBroadcastReceiver
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.Manifest.permission.*
import android.app.Activity
import android.os.Handler
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.yoavgibri.miniweather.models.OpenWeather
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val TAG = "MainActivity"
    private val REQUEST_CODE_SETTINGS = 5432
    lateinit var notificationManager :WeatherNotification

    private var permissionsOk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        versionTextView.text = "Ver " + BuildConfig.VERSION_NAME

        //askForPermissions()
        notificationManager = WeatherNotification(this)

        buttonSettings.setOnClickListener {
            startActivityForResult(Intent(this, SettingsActivity::class.java), REQUEST_CODE_SETTINGS)
        }
        registerSwitch.setOnCheckedChangeListener({ buttonView, isChecked ->
            if (mFusedLocationClient != null) {
                if (isChecked) {
                    if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
                        Do.setIsRegisteredForLocationUpdates(this, true)

                        requestLastLocationAndUpdateWeather()

                        mFusedLocationClient?.requestLocationUpdates(LocationHelper.createLocationRequest(), getPendingIntent())

                        Log.i(TAG, "MainActivity - registerSwitch OnClick - Checked")
                    } else {
                        askForPermissions()
                        registerSwitch.isChecked = false
                    }
                } else {
                    Do.setIsRegisteredForLocationUpdates(this, false)
                    mFusedLocationClient?.removeLocationUpdates(getPendingIntent())
                    notificationManager.cancelNotification()
                    Log.i(TAG, "MainActivity - registerSwitch OnClick - UnChecked")
                }
            } else {
                askForPermissions()
                registerSwitch.isChecked = false
            }
        })

        registerSwitch.isChecked = Do.getIsRegisteredForLocationUpdates(this)




        explanationTextView.setOnLongClickListener {
            val devIntent = Intent(this, DevActivity::class.java)
            startActivity(devIntent)
            return@setOnLongClickListener true
        }
    }

    private fun requestLastLocationAndUpdateWeather() {
        if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener {
                if (it != null) {
                    WeatherManager(this).getCurrentWeatherJson(it.latitude, it.longitude, object : WeatherManager.OnWeatherLoad {
                        override fun onWeather(weather: OpenWeather) {
                            notificationManager.updateWeather(weather)
                        }
                    })
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Handler().postDelayed({ registerSwitch.isChecked = true }, 1000)
    }


    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun askForPermissions(): Boolean {
        val permissionsList = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE, INTERNET)

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
                } else {
                    AlertDialog.Builder(this).setTitle("Permissions").setMessage("Hi there.\nWe need those permission in order to get weather updates, and to keep errors log.\n" +
                            "You won't be able to use this app unless you approve all of the permission.")
                            .setPositiveButton("go to permissions", { dialogInterface, i -> askForPermissions() })
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
        super.onActivityResult(requestCode, resultCode, data)
    }

}
