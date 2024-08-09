package com.yoavgibri.miniweather.managers

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.activities.SettingsActivity
import java.util.*

class SettingsManager {
    companion object {

        private val HOURS_12: String = App.context.resources.getStringArray(R.array.pref_time_format_values)[0]
        internal val HOURS_24: String = App.context.resources.getStringArray(R.array.pref_time_format_values)[1]

        private val METRIC: String = App.context.resources.getStringArray(R.array.pref_degrees_unit_values)[0]
        private val IMPERIAL: String = App.context.resources.getStringArray(R.array.pref_degrees_unit_values)[1]

        val pref: SharedPreferences = App.context.getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE)

        fun checkDefaultSettings() {
            // UNIT FORMAT
            if (pref.getString(App.context.getString(R.string.sp_key_units_format), "")!!.isEmpty()) {
                pref.edit().putString(App.context.getString(R.string.sp_key_units_format), getDefaultUnitSystem()).apply()
            }

            // TIME FORMAT
            if (pref.getString(App.context.getString(R.string.sp_key_time_format), "")!!.isEmpty()) {
                pref.edit().putString(App.context.getString(R.string.sp_key_time_format),
                        if (DateFormat.is24HourFormat(App.context)) HOURS_24 else HOURS_12).apply()
            }

            // INTERVALS MINUTES
            if (pref.getInt(App.context.getString(R.string.sp_key_refresh_interval), 0) == 0) {
                pref.edit().putInt(App.context.getString(R.string.sp_key_refresh_interval),
                        App.context.resources.getInteger(R.integer.number_picker_default_value)).apply()
            }

        }

        fun getUnitFormat(): String = pref.getString(App.context.getString(R.string.sp_key_units_format), getDefaultUnitSystem())!!
        fun getTimeFormat(): String = pref.getString(App.context.getString(R.string.sp_key_time_format), HOURS_12)!!
        fun getIntervalsMinutes(): Int = pref.getInt(App.context.getString(R.string.sp_key_refresh_interval), 30)
//        fun getIsRegistered

        private fun getDefaultUnitSystem(): String {
            val currentLocale = Locale.getDefault()

            return when (currentLocale.country.toUpperCase(currentLocale)) {
                // UK, UK, Myanmar, Liberia,
                "US", "GB", "MM", "LR" -> IMPERIAL
                else -> METRIC

            }

        }


    }
}