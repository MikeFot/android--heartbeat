package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model

import android.os.Parcelable
import com.michaelfotiadis.heartbeat.bluetooth.model.BondState
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UiBondedDevice(
    val name: String,
    val address: String,
    val bondedStatus: BondState,
    val deviceType: DeviceType
) : Parcelable