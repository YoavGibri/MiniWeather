package com.yoavgibri.miniweather

import android.app.Activity
import android.content.pm.PackageManager

class Permissions {

    companion object {

        fun checkPermission(activity: Activity, permission: String, method: () -> Unit) {
            when {
                (permissionGranted(permission)) -> {
                    method.invoke()
                }
                else -> {
                    requestPermission(activity, permission)
                }
            }

        }

        private fun permissionGranted(permission: String): Boolean {
            return App.context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }

        private fun requestPermission(activity: Activity, permission: String) {
            activity.requestPermissions(arrayOf(permission), 1)
        }

    }


}