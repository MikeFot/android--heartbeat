package com.michaelfotiadis.heartbeat.bluetooth.interactor

import io.reactivex.disposables.Disposable

interface Cancellable {
    fun cancel()
    fun register(list: MutableList<Cancellable>)
}

class DisposableCancellable(private val disposable: Disposable) : Cancellable {
    override fun register(list: MutableList<Cancellable>) {
        list.add(this)
    }

    override fun cancel() {
        disposable.dispose()
    }
}