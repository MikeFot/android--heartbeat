package com.michaelfotiadis.heartbeat.repo

import androidx.lifecycle.MutableLiveData
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MessageRepo(private val executionThreads: ExecutionThreads) {

    val messageLiveData = MutableLiveData<String>()

    private var lastMessageTimestamp = 0L
    private val messageDelayMs = TimeUnit.MILLISECONDS.toMillis(100L)

    fun log(message: String) {
        executionThreads.messageScope.launch {
            if (System.currentTimeMillis() - lastMessageTimestamp < messageDelayMs) {
                delay(messageDelayMs)
            }
            messageLiveData.postValue(message)
        }
    }
}