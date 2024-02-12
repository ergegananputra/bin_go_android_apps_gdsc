package com.gdsc.bingo.model.nearby

import com.google.gson.annotations.SerializedName
class ModelLocation {
    @SerializedName("lat")
    var lat: Double = 0.0

    @SerializedName("lng")
    var lng: Double = 0.0
}