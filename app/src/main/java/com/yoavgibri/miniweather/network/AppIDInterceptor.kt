package com.yoavgibri.miniweather.network

import okhttp3.Interceptor
import okhttp3.Response

class AppIDInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val httpUrl = chain.request().url.newBuilder().addQueryParameter("APPID", "0234b7546d074c6839610dfd89210bee").build()
        return chain.proceed(chain.request().newBuilder().url(httpUrl).build()
        )
    }
}