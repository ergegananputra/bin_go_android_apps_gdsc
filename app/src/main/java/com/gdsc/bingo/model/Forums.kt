package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * [Forums] Version 1.0
 *
 *
 * Petunjuk Penggunaan untuk NavArgs:
 *
 *
 *      -   Untuk recreate timestamp dari seconds Long gunakan:
 *          val timestamp = Timestamp(*seconds, 0)
 *
 *
 *      -   Untuk recreate referencePath gunakan:
 *          val reference = FirebaseFirestore.getInstance().document(*StringPath)
 *
 *
 * Rules:
 * - Jika text lebih dari 500_000 character maka gunakan text file
 *   text file berupa link ke storage google cloud
 */
data class Forums(
    override var referencePath: DocumentReference? = null,
    var title : String? = null,
    var text : String? = null,
    var isUsingTextFile : Boolean = false,
    var textFilePath : String? = null,
    var videoLink : String? = null,
    var likeCount : Long = 0,
    var dislikeCount : Long = 0,
    var commentCount : Long = 0,
    var thumbnailPhotosUrl : String? = null,
    var author : DocumentReference? = null,
    var komentarHub : DocumentReference? = null,
    var createdAt : Timestamp? = null,
) : FireModel {

    override val table: String
        get() = "forums"

    val textLimit = 500_000

    private object Keys {
        const val title = "title"
        const val text = "text"
        const val isUsingTextFile = "is_using_text_file"
        const val textFilePath = "text_file_path"
        const val videoLink = "video_link"
        const val likeCount = "like_count"
        const val dislikeCount = "dislike_count"
        const val commentCount = "comment_count"
        const val thumbnailPhotosUrl = "thumbnail_photos_url"
        const val author = "author"
        const val komentarHub = "komentar_hub"
    }

    override fun toFirebaseModel() = hashMapOf(
        FireModel.Keys.referencePath to referencePath,
        Keys.title to title,
        Keys.text to text,
        Keys.isUsingTextFile to isUsingTextFile,
        Keys.textFilePath to textFilePath,
        Keys.videoLink to videoLink,
        Keys.likeCount to likeCount,
        Keys.dislikeCount to dislikeCount,
        Keys.commentCount to commentCount,
        Keys.thumbnailPhotosUrl to thumbnailPhotosUrl,
        Keys.author to author,
        Keys.komentarHub to komentarHub,
        FireModel.Keys.createdAt to createdAt
        )

    override fun toModels(querySnapshot: QuerySnapshot): List<Forums> {
        return querySnapshot.mapNotNull {data ->
            Forums(
                referencePath = data[FireModel.Keys.referencePath] as? DocumentReference,
                title =data[Keys.title] as? String,
                text = data[Keys.text] as? String,
                isUsingTextFile = data[Keys.isUsingTextFile] as? Boolean ?: false,
                textFilePath = data[Keys.textFilePath] as? String,
                videoLink = data[Keys.videoLink] as? String,
                likeCount = data[Keys.likeCount] as? Long ?: 0,
                dislikeCount = data[Keys.dislikeCount] as? Long ?: 0,
                commentCount = data[Keys.commentCount] as? Long ?: 0,
                thumbnailPhotosUrl = data[Keys.thumbnailPhotosUrl] as? String,
                author = data[Keys.author] as? DocumentReference,
                komentarHub = data[Keys.komentarHub] as? DocumentReference,
                createdAt = data[FireModel.Keys.createdAt] as? Timestamp
            )
        }
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): Forums {
        return Forums(
            referencePath = documentSnapshot.getDocumentReference(FireModel.Keys.referencePath),
            title = documentSnapshot.getString(Keys.title),
            text = documentSnapshot.getString(Keys.text),
            isUsingTextFile = documentSnapshot.getBoolean(Keys.isUsingTextFile) ?: false,
            textFilePath = documentSnapshot.getString(Keys.textFilePath),
            videoLink = documentSnapshot.getString(Keys.videoLink),
            likeCount = documentSnapshot.getLong(Keys.likeCount) ?: 0,
            dislikeCount = documentSnapshot.getLong(Keys.dislikeCount) ?: 0,
            commentCount = documentSnapshot.getLong(Keys.commentCount) ?: 0,
            thumbnailPhotosUrl = documentSnapshot.getString(Keys.thumbnailPhotosUrl),
            author = documentSnapshot.getDocumentReference(Keys.author),
            komentarHub = documentSnapshot.getDocumentReference(Keys.komentarHub),
            createdAt = documentSnapshot.getTimestamp(FireModel.Keys.createdAt)
        )
    }
}
