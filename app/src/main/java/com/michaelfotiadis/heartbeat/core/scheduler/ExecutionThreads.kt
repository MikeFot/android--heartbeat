package com.michaelfotiadis.heartbeat.core.scheduler

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

class ExecutionThreads @Inject constructor() {
    val bleScheduler = Schedulers.single()
    val mainScheduler = AndroidSchedulers.mainThread()
    val jobScope = CoroutineScope(Job())
    val messageScope = CoroutineScope(Dispatchers.Main + Job())
}
