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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

    private var lastVisible: DocumentSnapshot? = null
    private var endOfList : DocumentSnapshot? = null

    private val tempObj = Forums()

    private val firestore = FirebaseFirestore.getInstance()

    private val baseQuery = firestore.collection(tempObj.table)
        .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
        .limit(10)

    fun pullLatestData(limit: Long = 50) {
        viewModelScope.launch(Dispatchers.IO) {
            val query = firestore.collection(tempObj.table)
                .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
                .limit(limit)

            query.get(Source.SERVER)
                .addOnSuccessListener { result ->
                    if (result.isEmpty) return@addOnSuccessListener

                    val forums = Forums().toModels(result)

                    writeUpdateRealm(forums)
                }
                .addOnFailureListener {
                    Log.e("KomunitasViewModel", "Error getting documents: ", it)
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
    fun loadMoreRecyclerData() {
        viewModelScope.launch(Dispatchers.IO) {

            if (lastVisible == null) {
                Log.e("KomunitasViewModel", "Last visible is null on loadMoreRecyclerData()")
                throw Exception("Wrong Action, last visible is null. \nYou should not call this " +
                        "function before refreshRecyclerData() is called")
            }

            if (lastVisible == endOfList) {
                Log.i("KomunitasViewModel", "Reach End of list")
                return@launch
            }

            val query = baseQuery.startAfter(lastVisible!!)
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
                    type = data.type
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
                }
            }
        }
    }

}