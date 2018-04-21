package com.yoavgibri.miniweather

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.yoavgibri.miniweather.broadcastReceivers.AlarmReceiver

class AlarmManagerHelper() {
    private val REQUEST_CODE_ALARM_MANAGER: Int = 58764
    lateinit var alarmMgr: AlarmManager
    lateinit var alarmIntent: PendingIntent


    constructor(context: Context) : this(){
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM_MANAGER, intent, 0)
    }

    private val interval: Long = 30 * 60 * 1000

    fun setRecurringAlarm(){
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, alarmIntent)
    }

    fun cancelAlarm(){
        alarmMgr.cancel(alarmIntent)
    }



}