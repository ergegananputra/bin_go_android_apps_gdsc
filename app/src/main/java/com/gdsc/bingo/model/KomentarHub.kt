package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * [KomentarHub] Version 1.0
 *
 *
 * [Forums] -> [KomentarHub] -> [Komentar]
 *
 *
 * [KomentarHub] memiliki sub-collection, yaitu: [Komentar]
 *
 *
 * Rules:
 * - Komentar ini terhubung dengan [Forums]
 * - Id dari komentar sesuai dengan postingan forum
 *
 *
 */
data class KomentarHub(
    override var referencePath: DocumentReference? = null,
    var createdAt : Timestamp? = null
) : FireModel {
    override val table: String
        get() = "komentar_hub"

    override fun toFirebaseModel() = hashMapOf(
        FireModel.Keys.referencePath to referencePath,
        FireModel.Keys.createdAt to createdAt
    )

    override fun toModels(querySnapshot: QuerySnapshot): List<KomentarHub> {
        return querySnapshot.documents.map {
            KomentarHub(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference,
                createdAt = it[FireModel.Keys.createdAt] as Timestamp
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): KomentarHub {
        return KomentarHub(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            createdAt = documentSnapshot.getTimestamp(FireModel.Keys.createdAt)
        )
    }
}