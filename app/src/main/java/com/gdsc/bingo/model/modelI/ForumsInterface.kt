package com.gdsc.bingo.model.modelI

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

interface ForumsInterface {
    var title : String?
    var text : String?
    var isUsingTextFile : Boolean
    var textFilePath : String?
    var videoLink : String?
    var likeCount : Long
    var dislikeCount : Long
    var commentCount : Long
    var likesReference: DocumentReference?
    var thumbnailPhotosUrl : String?
    var author : DocumentReference?
    var komentarHub : DocumentReference?
    var createdAt : Timestamp?
    var type : String
}