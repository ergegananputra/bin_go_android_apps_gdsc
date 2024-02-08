package com.gdsc.bingo.model

interface FireModel {
    val table : String
    fun toFirebaseModel() : Map<String, Any?>
}