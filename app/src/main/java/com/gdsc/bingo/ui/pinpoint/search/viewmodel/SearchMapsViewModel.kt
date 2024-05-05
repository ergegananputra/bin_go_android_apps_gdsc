package com.gdsc.bingo.ui.pinpoint.search.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.gdsc.bingo.BuildConfig.MAPS_API_KEY
import com.gdsc.bingo.model.BinLocation
import com.gdsc.bingo.model.RemoteSettings
import com.gdsc.bingo.services.api.firebase.MapsDataSyncFirebase
import com.gdsc.bingo.services.networking.ApiService
import com.gdsc.bingo.services.networking.MapsApiInterface
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SearchMapsViewModel : ViewModel() {
    private val _modelResultsMutableLiveData = MutableLiveData<ArrayList<BinLocation>>()
    private lateinit var firestore: FirebaseFirestore
    val modelResultsMutableLiveData: LiveData<ArrayList<BinLocation>>
        get() = _modelResultsMutableLiveData

    fun setMarkerLocation(
        strLocation: String
    ) {
        firestore = FirebaseFirestore.getInstance()
        val mapsApiService: MapsApiInterface = ApiService.getMaps()

        CoroutineScope(Dispatchers.Default).launch {

            val remoteSettings = firestore.collection("remote_settings")
                .document(RemoteSettings.DOCUMENT_ID.key)
                .get(Source.SERVER)
                .await()

            val isMapsEnabled = remoteSettings.getBoolean(RemoteSettings.IS_MAPS_ENABLED.key) ?: false
            val isDeveloperMode = remoteSettings.getBoolean(RemoteSettings.IS_DEVELOPER_MODE.key) ?: false
            val isSyncMapsToFirebase = remoteSettings.getBoolean(RemoteSettings.IS_SYNC_MAPS_TO_FIREBASE.key) ?: false

            val queryTempatPembuanganSampah : MutableList<BinLocation> = mutableListOf()
            val queryTPS : MutableList<BinLocation> = mutableListOf()
            val queryBankSampah : MutableList<BinLocation> = mutableListOf()

            if (isMapsEnabled) {
                withContext(Dispatchers.IO) {
                    queryTempatPembuanganSampah.addAll(
                        searchPlaceMaps(
                            "Tempat Pembuangan Sampah",
                            mapsApiService,
                            strLocation
                        ) ?: emptyList()
                    )
                    queryTPS.addAll(
                        searchPlaceMaps(
                            "TPS",
                            mapsApiService,
                            strLocation
                        ) ?: emptyList()
                    )
                    queryBankSampah.addAll(
                        searchPlaceMaps(
                            "Bank Sampah",
                            mapsApiService,
                            strLocation
                        ) ?: emptyList()
                    )
                }

                if (isSyncMapsToFirebase) {
                    withContext(Dispatchers.IO) {
                        val binLocationList = mutableListOf<BinLocation>()
                        binLocationList.addAll(queryTempatPembuanganSampah)
                        binLocationList.addAll(queryTPS)
                        binLocationList.addAll(queryBankSampah)

                        MapsDataSyncFirebase
                            .getInstance()
                            .uploadDataToFirestore(binLocationList)
                    }
                }
            }

            withContext(Dispatchers.IO) {
                MapsDataSyncFirebase
                    .getInstance()
                    .startGeoHashingForDev(isDeveloperMode)
            }

            val fireBinPoint = withContext(Dispatchers.IO) {
                fetchBinPointData(strLocation) ?: emptyList()
            }

            _modelResultsMutableLiveData.postValue(
                arrayListOf<BinLocation>().apply {
                    if (isMapsEnabled) {
                        addAll(queryTempatPembuanganSampah)
                        addAll(queryTPS)
                        addAll(queryBankSampah)
                    }
                    addAll(fireBinPoint)
                }
            )


        }
    }



    private suspend fun fetchBinPointData(strLocation: String) : List<BinLocation>? {
        firestore = FirebaseFirestore.getInstance()
        val places = firestore.collection(BinLocation().table)

        val locationList = strLocation.split(",")
        val latitude = locationList[0].toDouble()
        val longitude = locationList[1].toDouble()

        /**
         * Get the bounds of the search area
         * Source : https://firebase.google.com/docs/firestore/solutions/geoqueries#kotlin+ktx_1
         */
        val center = GeoLocation(latitude, longitude)
        val radiusInM = 50.0 * 1000.0
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks : MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = places
                .orderBy(BinLocation.BinLocationFields.GEOHASH.fieldName)
                .startAt(b.startHash)
                .endAt(b.endHash)

            tasks.add(q.get())
        }

        return suspendCoroutine { continuation ->
            Tasks.whenAllComplete(tasks)
                .addOnCompleteListener {
                    val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
                    for (task in tasks) {
                        val snap = task.result
                        for (doc in snap!!.documents) {
                            val location = doc.getGeoPoint(BinLocation.BinLocationFields.LOCATION.fieldName) ?: continue
                            val lat = location.latitude
                            val lng = location.longitude

                            // We have to filter out a few false positives due to GeoHash
                            // accuracy, but most will match
                            val docLocation = GeoLocation(lat, lng)
                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc)
                            }
                        }
                    }

                    Log.i("SearchMapsViewModel", "fetchBinPointData: $matchingDocs")

                    // matchingDocs contains the results
                    continuation.resume(matchingDocs.mapNotNull {
                        BinLocation().toModel(it)
                    })
                }
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
            val latitude = modelResults.modelGeometry.modelLocation.lat
            val longitude = modelResults.modelGeometry.modelLocation.lng
            BinLocation(
                referencePath = null,
                name = modelResults.name,
                address = modelResults.vicinity,
                location = GeoPoint(latitude, longitude),
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