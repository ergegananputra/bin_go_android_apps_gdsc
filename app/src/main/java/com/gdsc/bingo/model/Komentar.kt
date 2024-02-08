package com.gdsc.bingo.model

import com.google.firebase.Timestamp

data class Komentar(
    var komentar : String? = null,
    var profilePicturePath : String? = null,
    var username : String? = null,
    var createdAt : Timestamp? = null,
) : FireModel {
    override val table: String
        get() = "komentar"

    override fun toFirebaseModel() = hashMapOf(
        "komentar" to komentar,
        "profile_picture_path" to profilePicturePath,
        "username" to username,
        "created_at" to createdAt
        )

}
