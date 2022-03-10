package com.sesac.firewaterinfo.common

import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.LoginResult
import com.sesac.firewaterinfo.common.data.PersonInfo
import com.sesac.firewaterinfo.common.data.SimpleFW
import okhttp3.ResponseBody
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

    @GET("/api/firewater/images")
    fun selectFWImage(): Call<ResponseBody>

    @GET("/api/firewater/person")
    fun selectMyInfo(
        @Query("digitalCode") digitalCode: Long
    ): Call<PersonInfo>

    @GET("/api/firewater/login")
    fun selectLoginInfo(
        @Query("digitalCode") digitalCode: Long,
        @Query("pw") pw: String
    ): Call<LoginResult>

}