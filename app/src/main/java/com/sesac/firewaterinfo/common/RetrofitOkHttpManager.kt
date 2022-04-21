package com.sesac.firewaterinfo.common

import android.util.Log
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.TARGET_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

object RetrofitOkHttpManager {

    private var okHttpClient: OkHttpClient

    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(TARGET_URL)

    val firewaterRESTService: FireWaterRestService
        get() = retrofitBuilder.build().create(FireWaterRestService::class.java)

    init {
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                val newRequest: Request = request.newBuilder()
                    .addHeader("Accept","application/json")
                    .build()
                chain.proceed(newRequest)
            }).addInterceptor(RetryInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        retrofitBuilder.client(okHttpClient) // OkHttp 와 연동
    }

    private class RetryInterceptor: Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            var response: Response = chain.proceed(request)
            var tryCount = 0
            val maxLimit = 2
            while (!response.isSuccessful && tryCount < maxLimit) {
                Log.e(MY_DEBUG_TAG, "요청 실패 - tryCount= $tryCount")
                tryCount++
                response = chain.proceed(request)
            }
            return response
        }
    }
}