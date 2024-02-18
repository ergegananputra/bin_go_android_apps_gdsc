package com.gdsc.bingo.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

data class BinLocation(
    override var referencePath: DocumentReference?,
    var name: String? = null,
    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var isOpen: Boolean? = null,
    var additionalInfo: Map<String, Any>? = null,
    var rating: Double? = null,
    var reviewCount: Long? = null
) : FireModel {
    constructor() : this(null)

    override val table: String
        get() = "places"

    object Keys {
        const val name = "name"
        const val address = "address"
        const val latitude = "latitude"
        const val longitude = "longitude"
        const val isOpen = "is_open"
        const val additionalInfo = "additional_info"
        const val rating = "rating"
        const val reviewCount = "review_count"
    }

    override fun toFirebaseModel(): Map<String, Any?> {
        return hashMapOf(
            FireModel.Keys.referencePath to referencePath,
            Keys.name to name,
            Keys.address to address,
            Keys.latitude to latitude,
            Keys.longitude to longitude,
            Keys.isOpen to isOpen,
            Keys.additionalInfo to additionalInfo,
            Keys.rating to rating,
            Keys.reviewCount to reviewCount
        )
    }

    override fun toModels(querySnapshot: QuerySnapshot): List<BinLocation> {
        return querySnapshot.documents.map {
            BinLocation(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference?,
                name = it.getString(Keys.name),
                address = it.getString(Keys.address),
                latitude = it.getDouble(Keys.latitude),
                longitude = it.getDouble(Keys.longitude),
                isOpen = it.getBoolean(Keys.isOpen),
                additionalInfo = it.get(Keys.additionalInfo) as Map<String, Any>?,
                rating = it.getDouble(Keys.rating),
                reviewCount = it.getLong(Keys.reviewCount)
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): BinLocation? {
        return BinLocation(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            name = documentSnapshot.getString(Keys.name),
            address = documentSnapshot.getString(Keys.address),
            latitude = documentSnapshot.getDouble(Keys.latitude),
            longitude = documentSnapshot.getDouble(Keys.longitude),
            isOpen = documentSnapshot.getBoolean(Keys.isOpen),
            additionalInfo = documentSnapshot.get(Keys.additionalInfo) as Map<String, Any>?,
            rating = documentSnapshot.getDouble(Keys.rating),
            reviewCount = documentSnapshot.getLong(Keys.reviewCount)
        )
    }
}