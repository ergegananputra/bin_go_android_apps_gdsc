package com.gdsc.bingo.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot

interface FireModel {
    var referencePath : DocumentReference?
    val table : String
    fun toFirebaseModel() : Map<String, Any?>

    fun toModel(querySnapshot: QuerySnapshot) : List<FireModel>

    object Keys {
        const val referencePath = "reference_path"
        const val createdAt = "created_at"
    }
}