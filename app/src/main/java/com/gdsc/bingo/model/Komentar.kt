package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot

/**
 * [Komentar] Version 1.0
 *
 *
 * [Forums] -> [KomentarHub] -> [Komentar]
 *
 *
 * [Komentar] merupakan sub-collection dari [KomentarHub]
 *
 *
 * Rules:
 * - Komentar ini terhubung dengan [Forums] melalui perantara [KomentarHub]
 * - Id dari komentar ini bebas
 *
 *
 *
 */
data class Komentar(
    override var referencePath: DocumentReference? = null,
    var komentar : String? = null,
    var profilePicturePath : String? = null,
    var username : String? = null,
    var createdAt : Timestamp? = null,
) : FireModel {
    override val table: String
        get() = "komentar"

    private object Keys {
        const val komentar = "komentar"
        const val profilePicturePath = "profile_picture_path"
        const val username = "username"
    }

    override fun toFirebaseModel() = hashMapOf(
        FireModel.Keys.referencePath to referencePath,
        Keys.komentar to komentar,
        Keys.profilePicturePath to profilePicturePath,
        Keys.username to username,
        FireModel.Keys.createdAt to createdAt
        )

    override fun toModel(querySnapshot: QuerySnapshot): List<Komentar> {
        return querySnapshot.documents.map {
            Komentar(
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference,
                komentar = it[Keys.komentar] as String,
                profilePicturePath = it[Keys.profilePicturePath] as String,
                username = it[Keys.username] as String,
                createdAt = it[FireModel.Keys.createdAt] as Timestamp
            )
        }
    }

}
