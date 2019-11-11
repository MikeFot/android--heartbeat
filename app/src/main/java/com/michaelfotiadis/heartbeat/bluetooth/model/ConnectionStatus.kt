package com.michaelfotiadis.heartbeat.bluetooth.model

import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice

sealed class ConnectionStatus(
    val rxBleDevice: RxBleDevice,
    val rxBleConnection: RxBleConnection? = null
) {

    class Connecting(rxBleDevice: RxBleDevice) : ConnectionStatus(rxBleDevice)

    class Connected(
        rxBleDevice: RxBleDevice,
        rxBleConnection: RxBleConnection
    ) : ConnectionStatus(rxBleDevice, rxBleConnection)

    class Authorised(
        rxBleDevice: RxBleDevice,
        rxBleConnection: RxBleConnection
    ) : ConnectionStatus(rxBleDevice, rxBleConnection)

    class ConnectedNoHeartRate(
        rxBleDevice: RxBleDevice,
        rxBleConnection: RxBleConnection
    ) : ConnectionStatus(rxBleDevice, rxBleConnection)

    class Disconnecting(rxBleDevice: RxBleDevice) : ConnectionStatus(rxBleDevice)

    class Disconnected(rxBleDevice: RxBleDevice) : ConnectionStatus(rxBleDevice)

    class Failed(
        rxBleDevice: RxBleDevice,
        val exception: Throwable
    ) : ConnectionStatus(rxBleDevice)
}
