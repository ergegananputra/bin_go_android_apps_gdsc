package com.gdsc.bingo.ui.form_post.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FormPostViewModel : ViewModel() {
    val description : MutableLiveData<String> = MutableLiveData()
}