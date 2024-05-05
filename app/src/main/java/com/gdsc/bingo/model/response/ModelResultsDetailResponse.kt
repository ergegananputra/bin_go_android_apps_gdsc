package com.gdsc.bingo.model.response

import com.gdsc.bingo.model.details.ModelDetail
import com.google.gson.annotations.SerializedName

class ModelResultsDetailResponse {
    @SerializedName("result")
    lateinit var modelDetail: ModelDetail
}