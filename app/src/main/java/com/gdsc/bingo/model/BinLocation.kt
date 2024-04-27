package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot

data class BinLocation(
    override var referencePath: DocumentReference?,
    var name: String? = null,
    var address: String? = null,
    var location: GeoPoint? = null,
    var geohash: String? = null,
    var isOpen: Boolean? = null,
    var additionalInfo: Map<String, Any>? = null,
    var rating: Double? = null,
    var reviewCount: Long? = null,
    var type : String? = null,
    var timestamp : Timestamp? = null
) : FireModel {
    constructor() : this(null)

    override val table: String
        get() = "places"

    object Keys {
        const val name = "name"
        const val address = "address"
        const val location = "location"
        const val geohash = "geohash"
        const val isOpen = "is_open"
        const val additionalInfo = "additional_info"
        const val rating = "rating"
        const val reviewCount = "review_count"
        const val type = "type"
        const val timestamp = "timestamp"
    }

    enum class BinLocationFields(val fieldName: String) {
        NAME("name"),
        ADDRESS("address"),
        LOCATION("location"),
        GEOHASH("geohash"),
        IS_OPEN("is_open"),
        ADDITIONAL_INFO("additional_info"),
        RATING("rating"),
        REVIEW_COUNT("review_count"),
        TYPE("type"),
        TIMESTAMP("timestamp")
    }

    enum class BinTypeCategory(val fieldName: String) {
        BIN("bin"),
        REPORT("report")
    }

    enum class BinAdditionalInfo(val fieldName: String) {
        FORUM_ID("forum_id"),
        REPORT_DESCRIPTION("report_description"),
        REPORT_DATE("report_date"),
        PLACES_ID("places_id")
    }
    override fun toFirebaseModel(): Map<String, Any?> {
        timestamp = if (timestamp == null) Timestamp.now() else timestamp
        return hashMapOf(
            FireModel.Keys.referencePath to referencePath,
            Keys.name to name,
            Keys.address to address,
            Keys.location to location,
            Keys.geohash to geohash,
            Keys.isOpen to isOpen,
            Keys.additionalInfo to additionalInfo,
            Keys.rating to rating,
            Keys.reviewCount to reviewCount,
            Keys.type to type,
            Keys.timestamp to timestamp
        )
    }

    override fun toModels(querySnapshot: QuerySnapshot): List<BinLocation> {
        return querySnapshot.documents.map {
            BinLocation(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference?,
                name = it.getString(Keys.name),
                address = it.getString(Keys.address),
                location = it.getGeoPoint(Keys.location),
                geohash = it.getString(Keys.geohash),
                isOpen = it.getBoolean(Keys.isOpen),
                additionalInfo = it.get(Keys.additionalInfo) as Map<String, Any>?,
                rating = it.getDouble(Keys.rating),
                reviewCount = it.getLong(Keys.reviewCount),
                type = it.getString(Keys.type),
                timestamp = it.getTimestamp(Keys.timestamp)
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): BinLocation? {
        return BinLocation(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            name = documentSnapshot.getString(Keys.name),
            address = documentSnapshot.getString(Keys.address),
            location = documentSnapshot.getGeoPoint(Keys.location),
            geohash = documentSnapshot.getString(Keys.geohash),
            isOpen = documentSnapshot.getBoolean(Keys.isOpen),
            additionalInfo = documentSnapshot.get(Keys.additionalInfo) as Map<String, Any>?,
            rating = documentSnapshot.getDouble(Keys.rating),
            reviewCount = documentSnapshot.getLong(Keys.reviewCount),
            type = documentSnapshot.getString(Keys.type),
            timestamp = documentSnapshot.getTimestamp(Keys.timestamp)
        )
    }
}