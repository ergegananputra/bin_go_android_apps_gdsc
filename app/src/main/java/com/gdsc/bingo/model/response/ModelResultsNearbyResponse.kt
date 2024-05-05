package com.gdsc.bingo.model.response

import com.gdsc.bingo.model.nearby.ModelResults
import com.google.gson.annotations.SerializedName

class ModelResultsNearbyResponse {
    @SerializedName("results")
    lateinit var modelResults: List<ModelResults>
}