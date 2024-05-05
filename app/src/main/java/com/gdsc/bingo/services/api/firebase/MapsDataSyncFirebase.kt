package com.gdsc.bingo.services.api.firebase

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.gdsc.bingo.model.BinLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * This class is used to sync data from Firebase Firestore to the local database
 * For Experimentation only
 */
class MapsDataSyncFirebase {
    private lateinit var firestore: FirebaseFirestore

    fun uploadDataToFirestore(binLocationList: MutableList<BinLocation>) {
        for (binLocation in binLocationList) {
            firestore = FirebaseFirestore.getInstance()
            val places = firestore.collection(BinLocation().table)

            val lat = binLocation.location?.latitude ?: continue
            val lng = binLocation.location?.longitude ?: continue

            val id = generateIDforPlaces(lat, lng)


            places.document(id).get(Source.SERVER).addOnSuccessListener {
                if (it.exists().not()) {
                    val geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))
                    binLocation.geohash = geohash
                    places.document(id).set(binLocation.toFirebaseModel())
                        .addOnSuccessListener {
                            Log.i("SearchMapsViewModel", "uploadDataToFirestore: $binLocation")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("SearchMapsViewModel", "uploadDataToFirestore: $exception")
                        }
                }
            }
        }
    }

    fun startGeoHashingForDev(isDevMode: Boolean) {
        if (isDevMode.not()) return

        firestore = FirebaseFirestore.getInstance()
        val places = firestore.collection(BinLocation().table)

        places.whereEqualTo(BinLocation.BinLocationFields.GEOHASH.fieldName, null)
            .get(Source.SERVER)
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { documentSnapshot ->
                    val binLocation = BinLocation().toModel(documentSnapshot)
                    val location = binLocation?.location
                    if (location != null) {
                        val geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude))
                        places.document(documentSnapshot.id)
                            .update(BinLocation.BinLocationFields.GEOHASH.fieldName, geohash)
                            .addOnSuccessListener {
                                Log.i("SearchMapsViewModel", "startGeoHashingForDev: $geohash")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("SearchMapsViewModel", "startGeoHashingForDev: $exception")
                            }
                    }
                }

            }
    }

    companion object {
        private var instance : MapsDataSyncFirebase? = null
        fun getInstance() : MapsDataSyncFirebase {
            if (instance == null) {
                instance = MapsDataSyncFirebase()
            }
            return instance!!
        }

        /**
         * Generate ID for Firebase Collection "places".
         * This ID is used for document ID in Firestore.
         *
         * @param lat Double
         * @param lng Double
         * @return String
         */
        fun generateIDforPlaces(lat : Double, lng : Double) : String {
            return "LAT${lat}LNG${lng}"
        }
    }

}