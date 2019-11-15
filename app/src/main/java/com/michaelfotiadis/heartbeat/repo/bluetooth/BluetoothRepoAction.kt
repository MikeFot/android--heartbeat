package com.michaelfotiadis.heartbeat.repo.bluetooth

sealed class BluetoothRepoAction {
    object Idle : BluetoothRepoAction()
    object Connecting : BluetoothRepoAction()
    data class Connected(val name: String? = "UNKNOWN") : BluetoothRepoAction()
    data class Disconnected(val name: String? = "UNKNOWN") : BluetoothRepoAction()
    object ConnectionFailed : BluetoothRepoAction()
    object ServicesDiscovered : BluetoothRepoAction()
    object AuthorisationNotified : BluetoothRepoAction()
    object AuthorisationStepOne : BluetoothRepoAction()
    object AuthorisationStepTwo : BluetoothRepoAction()
    object AuthorisationFailed : BluetoothRepoAction()
    object AuthorisationComplete : BluetoothRepoAction()
}