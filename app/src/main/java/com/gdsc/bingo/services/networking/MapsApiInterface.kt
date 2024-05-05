package com.gdsc.bingo.services.networking

import com.gdsc.bingo.model.response.ModelResultsDetailResponse
import com.gdsc.bingo.model.response.ModelResultsNearbyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApiInterface {
    @GET("place/nearbysearch/json")
    fun getDataResult(
        @Query("key") key: String,
        @Query("keyword") keyword: String,
        @Query("location") location: String,
        @Query("rankby") rankby: String
    ): Call<ModelResultsNearbyResponse>

    @GET("/place/details/json")
    fun getDetailResult(@Query("key") key: String,
                        @Query("placeid") placeid: String): Call<ModelResultsDetailResponse>
}