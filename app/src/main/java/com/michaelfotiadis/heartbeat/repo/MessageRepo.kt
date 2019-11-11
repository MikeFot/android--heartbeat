package com.michaelfotiadis.heartbeat.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MessageRepo(private val executionThreads: ExecutionThreads) {

    private val _messageLiveData = MutableLiveData<String>()
    private val _errorLiveData = MutableLiveData<String>()
    val messageLiveData: LiveData<String>
        get() = _messageLiveData
    val errorLiveData: LiveData<String>
        get() = _errorLiveData


    private var lastMessageTimestamp = 0L
    private val messageDelayMs = TimeUnit.MILLISECONDS.toMillis(100L)

    fun log(message: String) {
        executionThreads.messageScope.launch {
            if (System.currentTimeMillis() - lastMessageTimestamp < messageDelayMs) {
                delay(messageDelayMs)
            }
            _messageLiveData.postValue(message)
        }
    }

    fun logError(message: String) {
        executionThreads.messageScope.launch {
            if (System.currentTimeMillis() - lastMessageTimestamp < messageDelayMs) {
                delay(messageDelayMs)
            }
            _errorLiveData.postValue(message)
        }
    }
}