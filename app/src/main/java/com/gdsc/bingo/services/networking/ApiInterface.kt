package com.gdsc.bingo.services.networking

import com.gdsc.bingo.model.response.ModelResultsDetail
import retrofit2.http.GET
import com.gdsc.bingo.model.response.ModelResultsNearby
import retrofit2.Call
import retrofit2.http.Query
interface ApiInterface {
    @GET("place/nearbysearch/json")
    fun getDataResult(
        @Query("key") key: String,
        @Query("keyword") keyword: String,
        @Query("location") location: String,
        @Query("rankby") rankby: String
    ): Call<ModelResultsNearby>

    @GET("/place/details/json")
    fun getDetailResult(@Query("key") key: String,
                        @Query("placeid") placeid: String): Call<ModelResultsDetail>
}