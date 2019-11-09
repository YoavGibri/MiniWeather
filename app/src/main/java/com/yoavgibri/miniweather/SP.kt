package com.yoavgibri.miniweather

import android.preference.PreferenceManager

class SP {

    companion object {
        private fun pref() = PreferenceManager.getDefaultSharedPreferences(App.context)
        private fun edit() = pref().edit()


        fun getString(key: String): String = getString(key, "")

        fun getString(key: String, defValue: String): String = pref().getString(key, defValue)!!


        fun getInt(key: String, defValue: Int) = pref().getInt(key, defValue)

        fun getInt(key: String) = getInt(key, 0)


        fun getFloat(key: String, defValue: Float) = pref().getFloat(key, defValue)

        fun getFloat(key: String) = getFloat(key, 0f)



        fun putString(key: String, value: String) = edit().putString(key, value).commit()

        fun getBoolean(key: String, defValue: Boolean) = pref().getBoolean(key, defValue)


    }
}