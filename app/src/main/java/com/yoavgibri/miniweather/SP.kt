package com.yoavgibri.miniweather

import android.preference.PreferenceManager

class SP {

    companion object {
        private fun pref() = PreferenceManager.getDefaultSharedPreferences(App.context)
        private fun edit() = pref().edit()


        fun getString(key: String): String = getString(key, "")

        fun getString(key: String, defValue: String): String = pref().getString(key, defValue)!!


        fun getInt(key: String, defValue: Int) = pref().getInt(key, defValue)

        fun getInt(key :String) = getInt(key, 0)


        fun putString(key: String, value: String) = edit().putString(key, value).commit()


    }
}