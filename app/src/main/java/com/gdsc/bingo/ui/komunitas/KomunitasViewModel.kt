package com.gdsc.bingo.ui.komunitas

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc.bingo.model.Forums
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KomunitasViewModel : ViewModel() {
    private val _forum = MutableLiveData<List<Forums>>()
    val forum: MutableLiveData<List<Forums>>
        get() = _forum

    private val firestore = FirebaseFirestore.getInstance()
    fun refreshRecyclerData(forceUpdate : Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val source = if (forceUpdate) Source.SERVER else Source.DEFAULT
            val tempObj = Forums()

            firestore.collection(tempObj.table).get(source)
                .addOnSuccessListener { result ->
                    val forums = tempObj.toModel(result)
                    _forum.value = forums
                    Log.i("KomunitasViewModel", "Data refreshed with ${forums.size} items")
                }
                .addOnFailureListener { exception ->
                    Log.e("KomunitasViewModel", "Error getting documents: ", exception)
                }
        }

    }
}