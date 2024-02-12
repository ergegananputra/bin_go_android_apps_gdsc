package com.gdsc.bingo.model.response

import com.google.gson.annotations.SerializedName
import com.gdsc.bingo.model.nearby.ModelResults
class ModelResultsNearby {
    @SerializedName("results")
    lateinit var modelResults: List<ModelResults>
}