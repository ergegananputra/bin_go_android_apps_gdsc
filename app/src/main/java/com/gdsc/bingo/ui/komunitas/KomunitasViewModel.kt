package com.gdsc.bingo.ui.komunitas

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.gdsc.bingo.model.FireModel
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class KomunitasViewModel : ViewModel() {
    private val _forum = MutableLiveData<List<Forums>>()
    val forum: MutableLiveData<List<Forums>>
        get() = _forum

    private var lastVisible: DocumentSnapshot? = null
    private var endOfList : DocumentSnapshot? = null

    private val tempObj = Forums()

    private val firestore = FirebaseFirestore.getInstance()

    private val baseQuery = firestore.collection(tempObj.table)
        .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
        .limit(10)

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
                    _forum.value = forums
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
                    _forum.value = forums
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }
    }

    fun refreshRecyclerData(forceUpdate : Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val source = if (forceUpdate) Source.SERVER else Source.DEFAULT


            baseQuery.get(source)
                .addOnSuccessListener { result ->
                    if (result.isEmpty) return@addOnSuccessListener

                    val forums = tempObj.toModels(result)
                    _forum.value = forums

                    lastVisible = result.documents[result.size() - 1]

                    Log.i("KomunitasViewModel", "Data refreshed with ${forums.size} items")
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
            val oldList = _forum.asFlow().first()

            query.get()
                .addOnSuccessListener { result ->
                    val forums = tempObj.toModels(result)
                    _forum.value = oldList.plus(forums)

                    lastVisible = result.documents[result.size() - 1]

                    Log.i("KomunitasViewModel", "Data loadMoreRecyclerData with ${forums.size} items")
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }
    }
}