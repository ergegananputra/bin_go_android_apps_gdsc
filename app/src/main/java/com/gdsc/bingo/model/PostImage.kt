package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

data class PostImage(
    var path: String? = null,
    var filename : String? = null,
    var createAt : Timestamp? = null,
    override var referencePath: DocumentReference? = null
) : FireModel {
    override val table: String
        get() = "images_post"

    object Keys {
        const val path = "path"
        const val filename = "filename"
    }
    override fun toFirebaseModel() =
         hashMapOf(
            Keys.path to path,
            Keys.filename to filename,
            FireModel.Keys.createdAt to createAt,
            FireModel.Keys.referencePath to referencePath
        )


    override fun toModels(querySnapshot: QuerySnapshot): List<PostImage> {
        return querySnapshot.documents.map {
            PostImage(
                path = it[Keys.path] as String,
                filename = it[Keys.filename] as String,
                createAt = it[FireModel.Keys.createdAt] as Timestamp,
                referencePath = it[FireModel.Keys.referencePath] as DocumentReference
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): PostImage {
        return PostImage(
            path = documentSnapshot.getString(Keys.path),
            filename = documentSnapshot.getString(Keys.filename),
            createAt = documentSnapshot.getTimestamp(FireModel.Keys.createdAt),
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath)
        )
    }

}
