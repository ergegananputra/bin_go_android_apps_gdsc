package com.gdsc.bingo.services.points

import com.google.firebase.firestore.DocumentReference

interface PointsRewardSystem {
    fun likePointRewards(user: DocumentReference, author: DocumentReference)
    fun unlikePointRewards(user: DocumentReference, author: DocumentReference)
    fun commentPointRewards(user: DocumentReference, author: DocumentReference)
}