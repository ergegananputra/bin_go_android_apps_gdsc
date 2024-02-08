package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Forums(
    var text : String? = null,
    var likeCount : Long = 0,
    var dislikeCount : Long = 0,
    var commentCount : Long = 0,
    var thumbnailPhotosUrl : String? = null,
    var author : DocumentReference? = null,
    var komentar : DocumentReference? = null,
    var createdAt : Timestamp? = null,
) : FireModel {
    override val table: String
        get() = "forums"

    override fun toFirebaseModel() = hashMapOf(
        "text" to text,
        "like_count" to likeCount,
        "dislike_count" to dislikeCount,
        "comment_count" to commentCount,
        "thumbnail_photos_url" to thumbnailPhotosUrl,
        "author" to author,
        "komentar" to komentar,
        "created_at" to createdAt
        )
}
