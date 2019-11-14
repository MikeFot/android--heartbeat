package com.michaelfotiadis.heartbeat.core.scheduler

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

class ExecutionThreads @Inject constructor() {
    val bleScheduler = Schedulers.io()
    val mainScheduler = AndroidSchedulers.mainThread()
    val jobScope = CoroutineScope(Dispatchers.IO)
    val messageScope = CoroutineScope(Dispatchers.Main + Job())
    val actionScope = CoroutineScope(Dispatchers.Main + Job())
}
