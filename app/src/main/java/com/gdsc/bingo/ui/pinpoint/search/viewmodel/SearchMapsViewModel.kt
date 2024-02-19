package com.gdsc.bingo.ui.pinpoint.search.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdsc.bingo.BuildConfig.MAPS_API_KEY
import com.gdsc.bingo.model.nearby.ModelResults
import com.gdsc.bingo.model.response.ModelResultsNearby
import com.gdsc.bingo.services.networking.ApiInterface
import com.gdsc.bingo.services.networking.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
class SearchMapsViewModel : ViewModel() {
    private val modelResultsMutableLiveData = MutableLiveData<ArrayList<ModelResults>>()
    private val strApiKey = MAPS_API_KEY

    fun setMarkerLocation(strLocation: String) {
        val apiService: ApiInterface = ApiService.getMaps()

        val combinedResults = ArrayList<ModelResults>()

        // Hitung jumlah panggilan API yang telah selesai
        var completedCalls = 0

        // Mencari Tempat Pembuangan
        val callTePeEs = apiService.getDataResult(strApiKey, "Tempat Pembuangan Sampah", strLocation, "distance")
        callTePeEs.enqueue(object : Callback<ModelResultsNearby> {
            override fun onResponse(call: Call<ModelResultsNearby>, response: Response<ModelResultsNearby>) {
                if (response.isSuccessful && response.body() != null) {
                    combinedResults.addAll(response.body()!!.modelResults)
                    completedCalls++

                    // Jika kedua panggilan telah selesai, maka kita bisa mem-post data ke modelResultsMutableLiveData
                    if (completedCalls == 2) {
                        modelResultsMutableLiveData.postValue(combinedResults)
                    }
                } else {
                    Log.e("responseTePeEs", response.toString())
                }
            }

            override fun onFailure(call: Call<ModelResultsNearby>, t: Throwable) {
                Log.e("failureTePeEs", t.toString())
            }
        })

        // Mencari TPS
        val callTPS = apiService.getDataResult(strApiKey, "TPS", strLocation, "distance")
        callTPS.enqueue(object : Callback<ModelResultsNearby> {
            override fun onResponse(call: Call<ModelResultsNearby>, response: Response<ModelResultsNearby>) {
                if (response.isSuccessful && response.body() != null) {
                    combinedResults.addAll(response.body()!!.modelResults)
                    completedCalls++

                    // Jika kedua panggilan telah selesai, maka kita bisa mem-post data ke modelResultsMutableLiveData
                    if (completedCalls == 2) {
                        modelResultsMutableLiveData.postValue(combinedResults)
                    }
                } else {
                    Log.e("responseTPS", response.toString())
                }
            }

            override fun onFailure(call: Call<ModelResultsNearby>, t: Throwable) {
                Log.e("failureTPS", t.toString())
            }
        })
    }

    fun getMarkerLocation(): LiveData<ArrayList<ModelResults>> = modelResultsMutableLiveData
}