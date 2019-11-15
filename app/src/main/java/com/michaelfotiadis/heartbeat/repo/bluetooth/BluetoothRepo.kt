package com.michaelfotiadis.heartbeat.repo.bluetooth

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import com.chibatching.kotpref.KotprefModel
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceInfo
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceResult
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class BluetoothRepo : KotprefModel() {

    private val actionPublisher = PublishSubject.create<BluetoothRepoAction>()
        .apply {
            onNext(BluetoothRepoAction.Idle)
        }

    val deviceInfoLiveData = MutableLiveData<DeviceInfo>()
        .apply {
            value = DeviceInfo()
        }

    fun actionAsFlowable(): Flowable<BluetoothRepoAction> {
        return actionPublisher.toFlowable(BackpressureStrategy.LATEST)
    }

    fun actionAsLiveData(): LiveData<BluetoothRepoAction> {
        return LiveDataReactiveStreams.fromPublisher(actionAsFlowable())
    }

    fun postAction(action: BluetoothRepoAction) {
        actionPublisher.onNext(action)
    }

    val bondedDevicesLiveData = MutableLiveData<Set<DeviceResult>>()
        .apply { postValue(setOf()) }
    val scanStatusLiveData = MutableLiveData<ScanStatus>()

    var connectedMacAddress by nullableStringPref(default = null)

    val bluetoothConnectionLiveData =
        BluetoothConnectionLiveData(context)

}