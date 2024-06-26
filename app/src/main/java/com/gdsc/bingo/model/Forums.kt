package com.gdsc.bingo.model

import com.gdsc.bingo.model.modelI.ForumsInterface
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
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
    override var title : String? = null,
    override var text : String? = null,
    override var isUsingTextFile : Boolean = false,
    override var textFilePath : String? = null,
    override var videoLink : String? = null,
    override var likeCount : Long = 0,
    override var dislikeCount : Long = 0,
    override var commentCount : Long = 0,
    override var likesReference: DocumentReference? = null,
    override var thumbnailPhotosUrl : String? = null,
    override var author : DocumentReference? = null,
    override var komentarHub : DocumentReference? = null,
    override var createdAt : Timestamp? = null,
    override var type : String = ForumType.ARTICLE.fieldName,
    var vicinity: GeoPoint? = null
) : FireModel, ForumsInterface {

    override val table: String
        get() = "forums"

    val textLimit = 500_000

    object Keys {
        const val title = "title"
        const val text = "text"
        const val isUsingTextFile = "is_using_text_file"
        const val textFilePath = "text_file_path"
        const val videoLink = "video_link"
        const val likeCount = "like_count"
        const val dislikeCount = "dislike_count"
        const val likesReference = "likes_reference"
        const val commentCount = "comment_count"
        const val thumbnailPhotosUrl = "thumbnail_photos_url"
        const val author = "author"
        const val komentarHub = "komentar_hub"
        const val type = "type"
        const val vicinity = "vicinity"
    }

    enum class ForumFields(val fieldName: String) {
        TITLE("title"),
        TEXT("text"),
        IS_USING_TEXT_FILE("is_using_text_file"),
        TEXT_FILE_PATH("text_file_path"),
        VIDEO_LINK("video_link"),
        LIKE_COUNT("like_count"),
        DISLIKE_COUNT("dislike_count"),
        LIKES_REFERENCE("likes_reference"),
        COMMENT_COUNT("comment_count"),
        THUMBNAIL_PHOTOS_URL("thumbnail_photos_url"),
        AUTHOR("author"),
        KOMENTAR_HUB("komentar_hub"),
        TYPE("type"),
        VICINITY("vicinity"),
    }

    enum class ForumType(val fieldName: String) {
        REPORT("report"),
        ARTICLE("article"),
        TIPS_AND_TRICKS("tips_and_tricks"),
        WASTE_MANAGEMENT_EDUCATION("waste_management_education"),
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
        Keys.likesReference to likesReference,
        Keys.commentCount to commentCount,
        Keys.thumbnailPhotosUrl to thumbnailPhotosUrl,
        Keys.author to author,
        Keys.komentarHub to komentarHub,
        FireModel.Keys.createdAt to createdAt,
        ForumFields.TYPE.fieldName to type,
        ForumFields.VICINITY.fieldName to vicinity
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
                likesReference = data[Keys.likesReference] as? DocumentReference,
                commentCount = data[Keys.commentCount] as? Long ?: 0,
                thumbnailPhotosUrl = data[Keys.thumbnailPhotosUrl] as? String,
                author = data[Keys.author] as? DocumentReference,
                komentarHub = data[Keys.komentarHub] as? DocumentReference,
                createdAt = data[FireModel.Keys.createdAt] as? Timestamp,
                type = data[ForumFields.TYPE.fieldName] as? String ?: ForumType.ARTICLE.fieldName,
                vicinity = data[ForumFields.VICINITY.fieldName] as? GeoPoint
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
            likesReference = documentSnapshot.getDocumentReference(Keys.likesReference),
            commentCount = documentSnapshot.getLong(Keys.commentCount) ?: 0,
            thumbnailPhotosUrl = documentSnapshot.getString(Keys.thumbnailPhotosUrl),
            author = documentSnapshot.getDocumentReference(Keys.author),
            komentarHub = documentSnapshot.getDocumentReference(Keys.komentarHub),
            createdAt = documentSnapshot.getTimestamp(FireModel.Keys.createdAt),
            type = documentSnapshot.getString(ForumFields.TYPE.fieldName) ?: ForumType.ARTICLE.fieldName,
            vicinity = documentSnapshot.getGeoPoint(ForumFields.VICINITY.fieldName)
        )
    }
}
