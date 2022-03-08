package com.sesac.firewaterinfo.common

import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.SimpleFW
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FireWaterRestService {

    @GET("/api/firewater/simple")
    fun requestSimpleFWSelect(
        @Query("latitudeL") latitudeL: Double,
        @Query("latitudeH") latitudeH: Double,
        @Query("longitudeL") longitudeL: Double,
        @Query("longitudeR") longitudeR: Double
    ): Call<List<SimpleFW>>

    @GET("/api/firewater/my")
    fun requestMyFWSelect(
        @Query("digitalCode") digitalCode: Long
    ): Call<List<AllFW>>

}