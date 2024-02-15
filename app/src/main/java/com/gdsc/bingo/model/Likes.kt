package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

data class Likes(
    override var referencePath: DocumentReference? = null,
    var updateAt : Timestamp? = null,
) : FireModel {
    override val table: String
        get() = "likes"

    private object Keys {
        const val updateAt = "update_at"
    }

    override fun toFirebaseModel(): Map<String, Any?> {
        return hashMapOf(
            FireModel.Keys.referencePath to referencePath,
            Keys.updateAt to updateAt
        )
    }

    override fun toModels(querySnapshot: QuerySnapshot): List<Likes> {
        return querySnapshot.documents.map {
            Likes(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference,
                updateAt = it[Keys.updateAt] as Timestamp
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): Likes {
        return Likes(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            updateAt = documentSnapshot.getTimestamp(Keys.updateAt)
        )
    }

}