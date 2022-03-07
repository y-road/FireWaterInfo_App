package com.sesac.firewaterinfo.common

import com.sesac.firewaterinfo.common.data.SimpleFW
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FireWaterRestService {

    @GET("/api/simple/firewater")
    fun requestSimpleFWSelect(
        @Query("latitudeL") latitudeL: Double,
        @Query("latitudeH") latitudeH: Double,
        @Query("longitudeL") longitudeL: Double,
        @Query("longitudeR") longitudeR: Double
    ): Call<List<SimpleFW>>

}