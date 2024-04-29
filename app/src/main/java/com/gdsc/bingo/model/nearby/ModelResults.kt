package com.gdsc.bingo.model.nearby

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ModelResults : Serializable {
    @SerializedName("geometry")
    lateinit var modelGeometry: ModelGeometry

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("vicinity")
    lateinit var vicinity: String

    @SerializedName("place_id")
    lateinit var placeId: String

    @SerializedName("rating")
    var rating = 0.0

    @SerializedName("is_open")
    var isOpen: Boolean? = null

    @SerializedName("type")
    lateinit var type: String
}