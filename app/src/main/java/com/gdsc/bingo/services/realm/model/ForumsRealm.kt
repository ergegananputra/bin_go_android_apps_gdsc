package com.gdsc.bingo.services.realm.model

import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.modelI.ForumsInterface
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.FullText
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.Date

class ForumsRealm : RealmObject, ForumsInterface {
    @PrimaryKey
    var referencePath : String = ""
    override var title: String? = null
    @FullText
    override var text: String? = null
    override var isUsingTextFile: Boolean = false
    override var textFilePath: String? = null
    override var videoLink: String? = null
    override var likeCount: Long = 0
    override var dislikeCount: Long = 0
    override var commentCount: Long = 0
    var likesReferencePath: String? = null
    override var thumbnailPhotosUrl: String? = null
    var authorPath: String? = null
    var komentarHubPath: String? = null
    var createdAtMillis: Long? = null
    override var type: String = Forums.ForumType.ARTICLE.fieldName
    var vicinity: GeoPointRealm? = null

    @Transient
    var reference: DocumentReference? = null
        get() {
            if (field == null && referencePath.isNotBlank()) {
                field = FirebaseFirestore.getInstance().document(referencePath)
            }
            return field
        }

    @Transient
    override var likesReference: DocumentReference? = null
        get() {
            if (field == null && likesReferencePath != null) {
                field = FirebaseFirestore.getInstance().document(likesReferencePath!!)
            }
            return field
        }

    @Transient
    override var author: DocumentReference? = null
        get() {
            if (field == null && authorPath != null) {
                field = FirebaseFirestore.getInstance().document(authorPath!!)
            }
            return field
        }

    @Transient
    override var komentarHub: DocumentReference? = null
        get() {
            if (field == null && komentarHubPath != null) {
                field = FirebaseFirestore.getInstance().document(komentarHubPath!!)
            }
            return field
        }

    @Transient
    override var createdAt: Timestamp? = null
        get() {
            if (field == null && createdAtMillis != null) {
                field = Timestamp(Date(createdAtMillis!!))
            }
            return field
        }
}