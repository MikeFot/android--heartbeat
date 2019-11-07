package com.michaelfotiadis.heartbeat.core.scheduler

import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject

class ExecutionThreads @Inject constructor() {
    val bleScheduler = Schedulers.single()
    val jobScope = CoroutineScope(Job())
}
