package com.michaelfotiadis.heartbeat.bluetooth.interactor

import io.reactivex.disposables.Disposable

interface Cancellable {
    fun cancel()
    fun isCancelled(): Boolean
}

class DisposableCancellable(private val disposable: Disposable) : Cancellable {

    override fun isCancelled(): Boolean {
        return disposable.isDisposed
    }

    override fun cancel() {
        disposable.dispose()
    }
}