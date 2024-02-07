package com.gdsc.bingo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.gson.annotations.SerializedName

data class Forums(
    @SerializedName("text")
    var text : String? = null,

    @SerializedName("like_count")
    var likeCount : Long = 0,

    @SerializedName("dislike_count")
    var dislikeCount : Long = 0,

    @SerializedName("comment_count")
    var commentCount : Long = 0,

    @SerializedName("thumbnail_photos_url")
    var thumbnailPhotosUrl : String? = null,

    @SerializedName("author")
    var author : DocumentReference? = null,

    @SerializedName("komentar")
    var komentar : DocumentReference? = null,

    @SerializedName("created_at")
    var createdAt : Timestamp? = null,
)
