package com.gdsc.bingo.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("username")
    var username : String? = null,

    @SerializedName("score")
    var score : Long = 0,

    @SerializedName("profile_picture_path")
    var profilePicturePath : String? = null,
)
