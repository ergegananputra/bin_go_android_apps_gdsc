package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * [Dislike] Version 1.0
 *
 *
 * Id/documentReference merupakan id user
 */
data class Dislike(
    override var referencePath: DocumentReference? = null,
    var owner: DocumentReference? = null,
    var objectPerson: DocumentReference? = null,
    var createAt : Timestamp? = null
) : FireModel {

    override val table: String
        get() = "dislike"

    private object Keys {
        const val owner = "owner"
        const val objectPerson = "object_person"
        const val createAt = "create_at"
    }
    override fun toFirebaseModel(): Map<String, Any?> {
        return hashMapOf(
            FireModel.Keys.referencePath to referencePath,
            Keys.owner to owner,
            Keys.objectPerson to objectPerson,
            Keys.createAt to createAt
        )
    }

    override fun toModels(querySnapshot: QuerySnapshot): List<Dislike> {
        return querySnapshot.documents.map {
            Dislike(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference,
                owner = it[Keys.owner] as DocumentReference,
                objectPerson = it[Keys.objectPerson] as DocumentReference,
                createAt = it[Keys.createAt] as Timestamp
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): Dislike {
        return Dislike(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            owner = documentSnapshot.getDocumentReference(Keys.owner),
            objectPerson = documentSnapshot.getDocumentReference(Keys.objectPerson),
            createAt = documentSnapshot.getTimestamp(Keys.createAt)
        )
    }

}