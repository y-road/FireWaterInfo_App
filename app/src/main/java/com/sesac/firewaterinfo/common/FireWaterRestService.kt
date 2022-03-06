package com.sesac.firewaterinfo.common

import com.sesac.firewaterinfo.common.data.SimpleFW
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FireWaterRestService {

    @GET("/api/simple/firewater")
    fun requestSimpleFWSelect(@Query("latitude") latitude: Double, @Query("longitude") longitude: Double): Call<List<SimpleFW>>

}