package com.yoavgibri.miniweather

import android.app.Application
import android.content.Context
import android.text.format.DateFormat
import com.yoavgibri.miniweather.activities.SettingsActivity
import timber.log.Timber

class App : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        context = applicationContext
    }




}