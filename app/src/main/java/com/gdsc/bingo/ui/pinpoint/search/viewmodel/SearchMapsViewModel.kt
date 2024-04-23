package com.gdsc.bingo.ui.pinpoint.search.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdsc.bingo.BuildConfig.MAPS_API_KEY
import com.gdsc.bingo.model.BinLocation
import com.gdsc.bingo.services.networking.ApiService
import com.gdsc.bingo.services.networking.MapsApiInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchMapsViewModel : ViewModel() {
    private val _modelResultsMutableLiveData = MutableLiveData<ArrayList<BinLocation>>()
    val modelResultsMutableLiveData: LiveData<ArrayList<BinLocation>>
        get() = _modelResultsMutableLiveData

//    private val strApiKey = MAPS_API_KEY

    fun setMarkerLocation(strLocation: String) {
        val mapsApiService: MapsApiInterface = ApiService.getMaps()
//
//        val combinedResults = ArrayList<ModelResults>()

        // Hitung jumlah panggilan API yang telah selesai
//        var completedCalls = 0

//        // Mencari Tempat Pembuangan
//        val callTePeEs = apiService.getDataResult(strApiKey, "Tempat Pembuangan Sampah", strLocation, "distance")
//        callTePeEs.enqueue(object : Callback<ModelResultsNearbyResponse> {
//            override fun onResponse(call: Call<ModelResultsNearbyResponse>, response: Response<ModelResultsNearbyResponse>) {
//                if (response.isSuccessful && response.body() != null) {
//                    combinedResults.addAll(response.body()!!.modelResults)
//                    completedCalls++
//
//                    // Jika kedua panggilan telah selesai, maka kita bisa mem-post data ke modelResultsMutableLiveData
//                    if (completedCalls == 2) {
//                        modelResultsMutableLiveData.postValue(combinedResults)
//                    }
//                } else {
//                    Log.e("responseTePeEs", response.toString())
//                }
//            }
//
//            override fun onFailure(call: Call<ModelResultsNearbyResponse>, t: Throwable) {
//                Log.e("failureTePeEs", t.toString())
//            }
//        })
//
//        // Mencari TPS
//        val callTPS = apiService.getDataResult(strApiKey, "TPS", strLocation, "distance")
//        callTPS.enqueue(object : Callback<ModelResultsNearbyResponse> {
//            override fun onResponse(call: Call<ModelResultsNearbyResponse>, response: Response<ModelResultsNearbyResponse>) {
//                if (response.isSuccessful && response.body() != null) {
//                    combinedResults.addAll(response.body()!!.modelResults)
//                    completedCalls++
//
//                    // Jika kedua panggilan telah selesai, maka kita bisa mem-post data ke modelResultsMutableLiveData
//                    if (completedCalls == 2) {
//                        modelResultsMutableLiveData.postValue(combinedResults)
//                    }
//                } else {
//                    Log.e("responseTPS", response.toString())
//                }
//            }
//
//            override fun onFailure(call: Call<ModelResultsNearbyResponse>, t: Throwable) {
//                Log.e("failureTPS", t.toString())
//            }
//        })

        CoroutineScope(Dispatchers.Default).launch {
            val queryTempatPembuanganSampah = withContext(Dispatchers.IO) {
                searchPlaceMaps("Tempat Pembuangan Sampah", mapsApiService, strLocation) ?: emptyList()
            }
            val queryTPS = withContext(Dispatchers.IO) {
                searchPlaceMaps("TPS", mapsApiService, strLocation) ?: emptyList()
            }
            val queryBankSampah = withContext(Dispatchers.IO) {
                searchPlaceMaps("Bank Sampah", mapsApiService, strLocation) ?: emptyList()
            }

            _modelResultsMutableLiveData.postValue(
                arrayListOf<BinLocation>().apply {
                    addAll(queryTempatPembuanganSampah)
                    addAll(queryTPS)
                    addAll(queryBankSampah)
                }
            )


        }
    }

    private suspend fun searchPlaceMaps(
        keyword: String,
        mapsApiInterface: MapsApiInterface,
        locationString : String,
        rankBy : String = "distance"
    ) : List<BinLocation>? {
        val response = withContext(Dispatchers.IO) {
            mapsApiInterface.getDataResult(
                MAPS_API_KEY,
                keyword,
                locationString,
                rankBy
            ).execute()
        }

        if (response.isSuccessful.not()) {
            Log.e("responseApiCall", response.toString())
            return null
        }

        val data = response.body()?.modelResults ?: run {
            Log.e("SeachMapsViewModel", "responseApiCall.body()?.modelResults is null")
            return null
        }

        Log.i("SearchMapsViewModel", "searchPlaceMaps: $data done fetching data")

        return data.map { modelResults ->
            BinLocation(
                referencePath = null,
                name = modelResults.name,
                address = modelResults.vicinity,
                latitude = modelResults.modelGeometry.modelLocation.lat,
                longitude = modelResults.modelGeometry.modelLocation.lng,
                additionalInfo = mapOf(
                    "place_id" to modelResults.placeId
                ),
                isOpen = modelResults.isOpen,
                rating = modelResults.rating,
                reviewCount = null
            )
        }
    }
}