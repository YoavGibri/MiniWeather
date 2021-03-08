package com.yoavgibri.miniweather

import android.content.Context
import android.content.SharedPreferences

class SP {

    companion object {
        private fun pref(): SharedPreferences {
            return App.context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        }

        private fun edit() = pref().edit()


        fun getString(key: String): String = getString(key, "")

        fun getString(key: String, defValue: String): String = pref().getString(key, defValue)!!


        fun getInt(key: String, defValue: Int) = pref().getInt(key, defValue)

        fun getInt(key: String) = getInt(key, 0)


        fun getFloat(key: String, defValue: Float) = pref().getFloat(key, defValue)

        fun getFloat(key: String) = getFloat(key, 0f)

        fun getBoolean(key: String, defValue: Boolean) = pref().getBoolean(key, defValue)


        fun putString(key: String, value: String) = edit().putString(key, value).commit()

        fun putBoolean(key: String, value: Boolean) = edit().putBoolean(key, value).commit()

        fun putFloat(key: String, value: Float) = edit().putFloat(key, value).commit()


    }
}