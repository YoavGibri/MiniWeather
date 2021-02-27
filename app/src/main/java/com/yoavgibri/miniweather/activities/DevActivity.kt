package com.yoavgibri.miniweather.activities

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.broadcastReceivers.LocationUpdatesBroadcastReceiver
import com.yoavgibri.miniweather.databinding.ActivityDevBinding
import com.yoavgibri.miniweather.models.OpenWeather


class DevActivity : AppCompatActivity() {
    companion object {
        val PERMISSIONS_REQUEST: Int = 12
    }

    private lateinit var binding: ActivityDevBinding
    private var currentLocation: CurrentLatLong? = null
    private val notification: WeatherNotification get() = WeatherNotification(this)
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val TAG: String = "DevActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_dev)

        initGps()
        initEvents()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.buttonStop.setText(if (notification.isShowing()) R.string.stop else R.string.play)


    }


    private fun initEvents() {
        binding.buttonStop.setOnClickListener {
            if (notification.isShowing()) {
                notification.cancelNotification()
                Do.logToFile("notification canceled!", this)
                binding.buttonStop.setText(R.string.play)
            } else {
                notification.show()
                Do.logToFile("notification set!", this)
                binding.buttonStop.setText(R.string.stop)
            }
        }

        binding.walkingManButton.setOnClickListener {
            notification.showIconOnly(R.drawable.animated_walking_man)
        }

        binding.weatherButton.setOnClickListener {
            WeatherManager(this).getCurrentWeather(object : WeatherManager.OnWeatherLoad {
                override fun onWeather(weather: OpenWeather) {
                    notification.updateWeather(weather)
                }
            })
        }

        binding.weatherButton.setOnLongClickListener {


            true }


        binding.registerLocationUpdatesButton.setOnClickListener {
            try {
//                if (LocationHelper.getRequestingLocationUpdates(this)) {
                if (notification.isShowing()) {
                    mFusedLocationClient?.removeLocationUpdates(getPendingIntent())
                    LocationHelper.setRequestingLocationUpdates(this, false)
                    Do.logToFile("Removing location updates", this)
                    binding.registerLocationUpdatesButton.text = getString(R.string.Reg_locations)
                } else {
                    mFusedLocationClient?.requestLocationUpdates(LocationHelper.createLocationRequest(), getPendingIntent())
                    LocationHelper.setRequestingLocationUpdates(this, true)
                    Do.logToFile("Starting location updates", this)
                    binding.registerLocationUpdatesButton.text = getString(R.string.un_Reg_locations)
                }

            } catch (e: SecurityException) {
                LocationHelper.setRequestingLocationUpdates(this, false)
                e.printStackTrace()
            }

        }

        //buttonCrash.setOnClickListener { Crashlytics.getInstance().crash() }
    }


    private fun getPendingIntent(): PendingIntent? {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
//            intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    fun onClick(v: View) {
        var icon = 0
        when (v.id) {
            R.id.buttonClouds -> icon = R.drawable.animated_clouds
            R.id.buttonCloudy -> icon = R.drawable.animated_cloudy
            R.id.buttonLightning -> icon = R.drawable.animated_lightning
            R.id.buttonRain -> icon = R.drawable.animated_rain
            R.id.buttonSun -> icon = R.drawable.animated_sun
        }
        notification.showIconOnly(icon)
    }


    private fun initGps() {

        currentLocation = CurrentLatLong()
        val status: Int? = currentLocation?.currentLatLong(this)

        if (status == 1) {
            Do.saveLocationToSharedPreferences(currentLocation?.currentLat!!, currentLocation?.currentLong!!)
        }

    }


}



