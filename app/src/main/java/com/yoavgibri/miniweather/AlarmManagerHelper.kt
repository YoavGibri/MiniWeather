package com.yoavgibri.miniweather

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.yoavgibri.miniweather.broadcastReceivers.AlarmReceiver

class AlarmManagerHelper(val context: Context) {
    companion object {
        private const val REQUEST_CODE_ALARM_MANAGER: Int = 58764
    }

    lateinit var alarmMgr: AlarmManager
    lateinit var alarmIntent: PendingIntent

    init {
        try {
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            alarmIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM_MANAGER, intent, 0)
        } catch (exc: Exception) {
            Do.logError(exc.message, context)
        }
    }



    private val intervalMultiplier: Long = 60 * 1000

    fun setRecurringAlarm() {
        val intervalMinutes : Int = SP.getInt(context.getString(R.string.sp_key_refresh_interval), context.resources.getInteger(R.integer.number_picker_default_value))
        setRecurringAlarm(intervalMinutes * intervalMultiplier)
    }

    fun setRecurringAlarm(intervalMilliSeconds : Long) {
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), intervalMilliSeconds, alarmIntent)
        Log.d("AlarmMangerHelper", "Alarm set to every ${(intervalMilliSeconds / 1000)} seconds")
    }

    fun cancelAlarm() {
        alarmMgr.cancel(alarmIntent)
    }


}