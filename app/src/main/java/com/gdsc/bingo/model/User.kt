package com.gdsc.bingo.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot

data class User(
    override var referencePath : DocumentReference? = null,
    var username : String? = null,
    var score : Long = 0,
    var profilePicturePath : String? = null,
) : FireModel {
    override val table : String
        get() = "user"

    private object Keys {
        const val username = "username"
        const val score = "score"
        const val profilePicturePath = "profile_picture_path"
    }

    override fun toFirebaseModel() = hashMapOf(
            FireModel.Keys.referencePath to referencePath,
            Keys.username to username,
            Keys.score to score,
            Keys.profilePicturePath to profilePicturePath
            )

    override fun toModel(querySnapshot: QuerySnapshot): List<User> {
        return querySnapshot.documents.map {
            User(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference,
                username = it[Keys.username] as String,
                score = it[Keys.score] as Long,
                profilePicturePath = it[Keys.profilePicturePath] as String
            )
        }
    }

}
