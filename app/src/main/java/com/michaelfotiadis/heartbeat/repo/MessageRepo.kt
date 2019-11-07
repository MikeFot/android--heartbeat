package com.michaelfotiadis.heartbeat.repo

import androidx.lifecycle.MutableLiveData

class MessageRepo {

    val messageLiveData = MutableLiveData<String>()

    fun log(message: String) {
        messageLiveData.postValue(message)
    }

}