package com.gdsc.bingo.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

interface FireModel {
    var referencePath : DocumentReference?
    val table : String
    fun toFirebaseModel() : Map<String, Any?>

    fun toModels(querySnapshot: QuerySnapshot) : List<FireModel>
    fun toModel(documentSnapshot: DocumentSnapshot) : FireModel?

    object Keys {
        const val referencePath = "reference_path"
        const val createdAt = "created_at"
    }
}