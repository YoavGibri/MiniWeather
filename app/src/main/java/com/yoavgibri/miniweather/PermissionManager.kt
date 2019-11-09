package com.yoavgibri.miniweather

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log

class Permissions {

    companion object {
        private fun permissionGranted(permission: String): Boolean {
            return App.context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }

        private fun requestPermission(permission: String, activity: Activity) {
            activity.requestPermissions(arrayOf(permission), 1)
        }

        fun checkPermission(permission: String, activity: Activity, method: () -> Unit) {
            if (permissionGranted(permission)) {
                method.invoke()
            } else requestPermission(permission, a(method))
        }

//        fun checkPermission(permission: String, foo: Function<>){
//
//        }

        class a(private val method: () -> Unit) : Activity() {

            override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
                when (requestCode) {
                    1 -> {
                        Log.d("inside", "yes")
                        method.invoke()
                    }
                }
            }
        }
    }


}