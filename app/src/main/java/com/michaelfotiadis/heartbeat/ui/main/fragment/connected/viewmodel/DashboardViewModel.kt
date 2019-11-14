package com.michaelfotiadis.heartbeat.ui.main.fragment.connected.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.core.livedata.SingleLiveEvent
import javax.inject.Inject

class DashboardViewModel : ViewModel() {

    val actionLiveData = SingleLiveEvent<Action>()

    fun onSingleHeartRateClicked() {
        actionLiveData.postValue(Action.SHOW_HEART_RATE_SINGLE)
    }

    fun onContinuousHeartRateClicked() {
        actionLiveData.postValue(Action.SHOW_HEART_RATE_CONTINUOUS)
    }

    fun onProfileClicked() {
        actionLiveData.postValue(Action.MY_PROFILE)
    }

    fun onDeviceInfoClicked() {
        actionLiveData.postValue(Action.SHOW_DEVICE_INFO)
    }
}

enum class Action {
    SHOW_HEART_RATE_SINGLE,
    SHOW_HEART_RATE_CONTINUOUS,
    MY_PROFILE,
    SHOW_DEVICE_INFO
}

class DashboardViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel() as T
    }
}


