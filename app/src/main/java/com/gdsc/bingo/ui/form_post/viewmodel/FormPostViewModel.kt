package com.gdsc.bingo.ui.form_post.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint

class FormPostViewModel : ViewModel() {
    val vicinity : MutableLiveData<GeoPoint> = MutableLiveData()
    var address : String? = null
    val description : MutableLiveData<String> = MutableLiveData()
}