package com.gdsc.bingo.model

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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

    override fun toModels(querySnapshot: QuerySnapshot): List<User> {
        return querySnapshot.documents.map {
            User(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference,
                username = it[Keys.username] as String,
                score = it[Keys.score] as Long,
                profilePicturePath = it[Keys.profilePicturePath] as String
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): User {
        return User(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            username = documentSnapshot.getString(Keys.username),
            score = documentSnapshot.getLong(Keys.score)!!,
            profilePicturePath = documentSnapshot.getString(Keys.profilePicturePath)
        ).also {
            Log.i("ArtikelFragment", "NavArgs" +
                    "\n\tReferencePath: ${it.referencePath}" +
                    "\n\tUsername: ${it.username}" +
                    "\n\tScore: ${it.score}" +
                    "\n\tProfilePicturePath: ${it.profilePicturePath}"
            )
        }
    }

}
