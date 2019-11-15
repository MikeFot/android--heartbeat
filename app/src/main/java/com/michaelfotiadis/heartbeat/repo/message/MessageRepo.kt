package com.michaelfotiadis.heartbeat.repo.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject


class MessageRepo {

    private val messageSubject = PublishSubject.create<String>()
    private val errorSubject = PublishSubject.create<String>()

    fun log(message: String) {
        messageSubject.onNext(message)
    }

    fun logError(message: String) {
        errorSubject.onNext(message)
    }

    fun getMessageLiveData(): LiveData<String> {
        return LiveDataReactiveStreams.fromPublisher(
            messageSubject
                .toFlowable(BackpressureStrategy.BUFFER)
        )
    }

    fun getErrorLiveData(): LiveData<String> {
        return LiveDataReactiveStreams.fromPublisher(
            errorSubject
                .toFlowable(BackpressureStrategy.BUFFER)
        )
    }

}