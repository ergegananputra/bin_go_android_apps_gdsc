package com.gdsc.bingo.model.nearby

import com.google.gson.annotations.SerializedName
import com.gdsc.bingo.model.nearby.ModelLocation
class ModelGeometry {
    @SerializedName("location")
    lateinit var modelLocation: ModelLocation
}