package com.gdsc.bingo.model

data class User(
    var username : String? = null,
    var score : Long = 0,
    var profilePicturePath : String? = null,
) : FireModel {
    override val table : String
        get() = "user"
    override fun toFirebaseModel() = hashMapOf(
            "username" to username,
            "score" to score,
            "profile_picture_path" to profilePicturePath
            )

}
