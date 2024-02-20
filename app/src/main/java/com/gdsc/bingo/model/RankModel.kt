package com.gdsc.bingo.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

data class RankModel(
    override var referencePath: DocumentReference?

) : FireModel {
    override val table: String
        get() = "rank"

    override fun toFirebaseModel(): Map<String, Any?> {
        return kotlin.collections.hashMapOf()
    }

    override fun toModels(querySnapshot: QuerySnapshot): List<FireModel> {
        return kotlin.collections.emptyList()
    }

    override fun toModel(documentSnapshot: DocumentSnapshot): FireModel? {
        return null
    }
}
