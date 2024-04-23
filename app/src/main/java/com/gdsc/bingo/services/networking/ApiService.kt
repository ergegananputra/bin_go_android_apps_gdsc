package com.gdsc.bingo.services.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class ApiService {
    companion object {
        private const val BASE_URL_MAPS = "https://maps.googleapis.com/maps/api/"
        fun getMaps(): MapsApiInterface {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_MAPS)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(MapsApiInterface::class.java)
        }
    }
}