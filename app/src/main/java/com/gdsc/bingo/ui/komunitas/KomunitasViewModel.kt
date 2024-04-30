package com.gdsc.bingo.ui.komunitas

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc.bingo.BinGoApplication
import com.gdsc.bingo.model.FireModel
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.realm.model.ForumsRealm
import com.gdsc.bingo.services.realm.model.GeoPointRealm
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.suspendCoroutine

class KomunitasViewModel : ViewModel() {
    private val realm = BinGoApplication.realm

    val mostLikes = MutableLiveData<Forums>()
    val latestUserPost = MutableLiveData<Forums>()

    val forumsRealm  = realm
        .query<ForumsRealm>()
        .sort("createdAtMillis", Sort.DESCENDING)
        .asFlow().map { result ->
            result.list.toList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val forumsRealmReport = realm.
        query<ForumsRealm>(
            "type == $0",
            Forums.ForumType.REPORT.fieldName
        )
        .sort("createdAtMillis", Sort.DESCENDING)
        .asFlow().map { result ->
            result.list.toList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val forumsRealmTricks = realm.
        query<ForumsRealm>(
            "type == $0",
            Forums.ForumType.TIPS_AND_TRICKS.fieldName
        )
            .sort("createdAtMillis", Sort.DESCENDING)
            .asFlow().map { result ->
                result.list.toList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    val forumsRealmEducation = realm.
        query<ForumsRealm>(
            "type == $0",
            Forums.ForumType.WASTE_MANAGEMENT_EDUCATION.fieldName
        )
            .sort("createdAtMillis", Sort.DESCENDING)
            .asFlow().map { result ->
                result.list.toList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    private var lastVisible: DocumentSnapshot? = null
    private var endOfList : DocumentSnapshot? = null

    private val tempObj = Forums()

    private val firestore = FirebaseFirestore.getInstance()

    private val baseQuery = firestore.collection(tempObj.table)
        .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
        .limit(10)

    fun pullLatestData(limit: Long = 50, formType: Forums.ForumType = Forums.ForumType.ARTICLE) {
        viewModelScope.launch(Dispatchers.IO) {
            val query = firestore.collection(tempObj.table)
                .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
                .limit(limit)

            if (formType != Forums.ForumType.ARTICLE) {
                query.whereEqualTo(Forums.Keys.type, formType.fieldName)
            }

            query.get(Source.SERVER)
                .addOnSuccessListener { result ->
                    if (result.isEmpty) return@addOnSuccessListener

                    lastVisible = result.documents[result.size() - 1]

                    val forums = Forums().toModels(result)

                    writeUpdateRealm(forums)
                }
                .addOnFailureListener {
                    Log.e("KomunitasViewModel", "Error getting documents: ", it)
                }

        }

    }

    private suspend fun pullLatestDataCheck(formType: Forums.ForumType) : Boolean {
        return suspendCoroutine { continuation ->
            viewModelScope.launch(Dispatchers.IO) {
                val query = firestore.collection(tempObj.table)
                    .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
                    .limit(10)

                if (formType != Forums.ForumType.ARTICLE) {
                    query.whereEqualTo(Forums.Keys.type, formType.fieldName)
                }

                try {
                    val result = withContext(Dispatchers.IO) {
                        withTimeoutOrNull(10_000L) {
                            query.get(Source.SERVER).await()
                        }
                    }

                    if (result == null) {
                        continuation.resumeWith(Result.success(false))
                        return@launch
                    }


                    lastVisible = result.documents[result.size() - 1]

                    val forums = Forums().toModels(result)

                    writeUpdateRealm(forums)
                    continuation.resumeWith(Result.success(true))

                } catch (e: Exception) {
                    Log.e("KomunitasViewModel", "Error getting documents: ", e)
                    continuation.resumeWith(Result.failure(e))
                }

            }
        }
    }

    private fun writeUpdateRealm(data: List<Forums>) {
        viewModelScope.launch(Dispatchers.IO) {
            realm.write {
                data.toRealm().forEach {
                    copyToRealm(it, updatePolicy = UpdatePolicy.ALL)
                }
            }
        }
    }

    fun deleteBeyondLimit(limit: Int = 50) {
        viewModelScope.launch(Dispatchers.IO) {
            pullLatestData(limit.toLong())

            val cursor = forumsRealm.first()
            // delete data beyond limit
            if (cursor.size > limit) {
                val beyondLimit = cursor.drop(limit)
                realm.write {
                    beyondLimit.forEach {
                        it.deleteFromRealm()
                    }
                }
            }

        }
    }

    private fun ForumsRealm.deleteFromRealm() {
        viewModelScope.launch(Dispatchers.IO) {
            realm.write {
                delete(this@deleteFromRealm)
            }
        }
    }

    fun loadEndOfList() {
        viewModelScope.launch(Dispatchers.IO) {
            val query = firestore.collection(tempObj.table)
                .orderBy(FireModel.Keys.createdAt, Query.Direction.ASCENDING)
                .limit(1)
            query.get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) return@addOnSuccessListener

                    endOfList = result.documents[0]
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }
    }

    fun loadMostLikeData() {
        viewModelScope.launch(Dispatchers.IO) {
            val query = firestore.collection(tempObj.table)
                .orderBy(Forums.Keys.likeCount, Query.Direction.DESCENDING)
                .limit(1)
            query.get()
                .addOnSuccessListener { result ->
                    val forums = tempObj.toModels(result)
                    mostLikes.value = forums.first()
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }
    }

    fun loadLatestUserPost(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "${User().table}/$uid"
            val query = firestore.collection(tempObj.table)
                .whereEqualTo(Forums.Keys.author, firestore.document(path))
                .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
                .limit(1)
            query.get()
                .addOnSuccessListener { result ->
                    val forums = tempObj.toModels(result)
                    latestUserPost.value = forums.first()
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }
    }

    /**
     * Load more data to recycler view
     *
     *
     * This function will load more data to recycler view
     *
     *
     * @throws Exception if last visible is null
     */
    fun loadMoreRecyclerData(forumType: Forums.ForumType) {
        viewModelScope.launch(Dispatchers.IO) {

            if (lastVisible == null) {
                Log.e("KomunitasViewModel", "Last visible is null on loadMoreRecyclerData()")

                Log.i("KomunitasViewModel", "initiative Refreshing recycler data")
                val result = withContext(Dispatchers.IO) {
                    pullLatestDataCheck(forumType)
                }

                try {
                    if (result) {
                        Log.i("KomunitasViewModel", "Data is loaded, retrying loadMoreRecyclerData")
                        return@launch
                    } else {
                        Log.i("KomunitasViewModel", "There is no data to loadMoreRecyclerData")
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("KomunitasViewModel", "Error getting documents: ", e)
                    return@launch
                }
            }

            if (lastVisible == endOfList) {
                Log.i("KomunitasViewModel", "Reach End of list")
                return@launch
            }

            val query = baseQuery.startAfter(lastVisible!!)

            if (forumType != Forums.ForumType.ARTICLE) {
                query.whereEqualTo(Forums.Keys.type, forumType.fieldName)
            }

            val oldList = forumsRealm.first().toModel()

            query.get()
                .addOnSuccessListener { result ->
                    val forums = tempObj.toModels(result)
                    val newList = oldList.plus(forums)
                    writeUpdateRealm(newList)

                    lastVisible = result.documents[result.size() - 1]

                    Log.i("KomunitasViewModel", "Data loadMoreRecyclerData with ${forums.size} items")
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }
    }

    suspend fun searchForums(
        keyword: String,
        forumType: String,
        limitToSearch : Long = 700,
        autoCancelTime : Long = 10_000L
    ) : List<Forums>? {
        return try {
            val fireQuery = firestore.collection(tempObj.table)
                .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
                .limit(limitToSearch)

            when(forumType) {
                Forums.ForumType.REPORT.fieldName -> fireQuery.whereEqualTo(Forums.Keys.type, forumType)
            }

            withTimeoutOrNull(autoCancelTime) {
                val result = fireQuery.get(Source.SERVER).await()

                if (result.isEmpty) return@withTimeoutOrNull

                val forums = Forums().toModels(result)
                writeUpdateRealm(forums)
            }

            val result = realm.query<ForumsRealm>(
                "(title LIKE[c] $0 or text TEXT $0) and type == $1",
                "*$keyword*", forumType
                )
                .sort("createdAtMillis", Sort.DESCENDING)
                .asFlow().map { result ->
                    result.list.toList()
                }

            val searchResult = result.first().toModel()

            searchResult.ifEmpty {
                Log.e("KomunitasViewModel", "No forums found with keyword: $keyword")
                null
            }
        } catch (e: Exception) {
            Log.e("KomunitasViewModel", "Error searching forums: ", e)
            null
        }
    }

    companion object {
        fun List<ForumsRealm>.toModel() : List<Forums> {
            return this.map { data ->
                Forums(
                    referencePath = data.reference,
                    title = data.title,
                    text = data.text,
                    isUsingTextFile = data.isUsingTextFile,
                    textFilePath = data.textFilePath,
                    videoLink = data.videoLink,
                    likeCount = data.likeCount,
                    dislikeCount = data.dislikeCount,
                    commentCount = data.commentCount,
                    likesReference = data.likesReference,
                    thumbnailPhotosUrl = data.thumbnailPhotosUrl,
                    author = data.author,
                    komentarHub = data.komentarHub,
                    createdAt = data.createdAt,
                    type = data.type,
                    vicinity = if (data.vicinity != null) {
                        GeoPoint(
                            data.vicinity!!.latitude,
                            data.vicinity!!.longitude
                        )
                    } else null
                )
            }
        }

        fun List<Forums>.toRealm() : List<ForumsRealm> {
            return this.map { data ->
                ForumsRealm().apply {
                    referencePath = data.referencePath!!.path
                    title = data.title
                    text = data.text
                    isUsingTextFile = data.isUsingTextFile
                    textFilePath = data.textFilePath
                    videoLink = data.videoLink
                    likeCount = data.likeCount
                    dislikeCount = data.dislikeCount
                    commentCount = data.commentCount
                    likesReferencePath = data.likesReference!!.path
                    thumbnailPhotosUrl = data.thumbnailPhotosUrl
                    authorPath = data.author!!.path
                    komentarHubPath = data.komentarHub!!.path
                    createdAtMillis = data.createdAt!!.toDate().time
                    type = data.type
                    vicinity = if (data.vicinity != null) {
                        GeoPointRealm().apply {
                            latitude = data.vicinity!!.latitude
                            longitude = data.vicinity!!.longitude
                        }
                    } else null
                }
            }
        }
    }

}