package com.example.culinarycompanion.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

// Data classes to match the JSON response from ImgBB
data class ImgbbResponse(
    @SerializedName("data") val data: ImgbbData,
    @SerializedName("success") val success: Boolean,
    @SerializedName("status") val status: Int
)

data class ImgbbData(
    @SerializedName("url") val url: String
)

// The Retrofit Interface that defines the API call
interface ImgbbApiService {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Response<ImgbbResponse>
}