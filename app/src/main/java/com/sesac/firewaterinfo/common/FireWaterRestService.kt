package com.sesac.firewaterinfo.common

import com.sesac.firewaterinfo.common.data.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface FireWaterRestService {

    @GET("/api/firewater/check-api")
    fun checkAPI(): Call<FireDefault>

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

    @GET("/api/firewater/one")
    fun selectOneFWDetail(
        @Query("number") number: Int
    ): Call<AllFW>

    @GET("/api/firewater/images")
    fun selectFWImage(): Call<ResponseBody>

    @GET("/api/firewater/person")
    fun selectMyInfo(
        @Query("digitalCode") digitalCode: Long
    ): Call<PersonInfo>

    @GET("/api/firewater/favorite")
    fun selectMyFavorites(
        @Query("digitalCode") digitalCode: Long
    ): Call<List<MyFavorites>>

    @GET("/api/firewater/search")
    fun selectSearch(
        @Query("name") name: String
    ): Call<List<MyFavorites>>

    @FormUrlEncoded
    @POST("/api/firewater/login")
    fun selectLoginInfo(
        @Field("digitalCode") digitalCode: Long,
        @Field("pw") pw: String
    ): Call<LoginResult>


    @FormUrlEncoded
    @POST("/api/firewater/sign")
    fun sendSignInfo(
        @Field("digitalCode") digitalCode: Long,
        @Field("name") name: String,
        @Field("rank") rank: String,
        @Field("belongStation") belongStation: String,
        @Field("belongCenter") belongCenter: String,
        @Field("belongPhone") belongPhone: String,
        @Field("password") password: String,
        @Field("imgName") imgName: MultipartBody.Part
    ): Call<SignInfo>

    @FormUrlEncoded
    @POST("/api/firewater/add/editlog")
    fun sendEditLog(
        @Field("digitalCode") digitalCode: Long,
        @Field("editedName") editedName: String,
        @Field("editedType") editedType: String,
        @Field("editedTime") editedTime: String
    ): Call<CntResult>

    @FormUrlEncoded
    @POST("/api/firewater/add/newfw")
    fun sendEditFW(
        @Field("name") name: String,
        @Field("serial") serial: Int,
        @Field("type") type: String,
        @Field("address") address: String,
        @Field("detail") detail: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("center") center: String,
        @Field("protectCase") protectCase: Boolean,
        @Field("available") available: Boolean,
        @Field("installationYear") installationYear: Int,
        @Field("pipeDepth") pipeDepth: Double,
        @Field("pipeDiameter") pipeDiameter: Double,
        @Field("outPressure") outPressure: Double,
        @Field("updateDate") updateDate: String,
        @Field("imgName") imgName: String
    ): Call<CntResult>

    @FormUrlEncoded
    @POST("/api/firewater/remove/oldfw")
    fun removeFW(
        @Field("number") name: Int,
    ): Call<CntResult>

    @FormUrlEncoded
    @POST("/api/firewater/add/favorite")
    fun addFavorite(
        @Field("digitalCode") digitalCode: Long,
        @Field("serial") serial: Int
    ): Call<CntResult>

    @FormUrlEncoded
    @POST("/api/firewater/remove/favorite")
    fun removeFavorite(
        @Field("number") number: Int
    ): Call<CntResult>

}