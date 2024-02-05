package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class Komentar(
    @SerializedName("komentar")
    var komentar : String? = null,

    @SerializedName("profile_picture_path")
    var profilePicturePath : String? = null,

    @SerializedName("username")
    var username : String? = null,

    @SerializedName("created_at")
    var createdAt : Timestamp? = null,
)
