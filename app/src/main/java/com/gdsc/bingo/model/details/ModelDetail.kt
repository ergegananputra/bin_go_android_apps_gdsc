package com.gdsc.bingo.model.details


import com.gdsc.bingo.model.nearby.ModelGeometry
import com.google.gson.annotations.SerializedName
class ModelDetail {
    @SerializedName("geometry")
    lateinit var modelGeometry: ModelGeometry

    @SerializedName("opening_hours")
    lateinit var modelOpening: ModelOpening

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("formatted_phone_number")
    lateinit var formatted_phone_number: String

    @SerializedName("rating")
    var rating = 0.0
}