package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.core.model.Optional
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.Timeout
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*
import java.util.concurrent.TimeUnit

class ConnectToMacInteractor(
    private val rxBleClient: RxBleClient,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(
        macAddress: String,
        isHeartRateServiceRequired: Boolean,
        callback: (ConnectionStatus) -> Unit
    ): DisposableCancellable {

        val device = rxBleClient.getBleDevice(macAddress)
        val operation1 = device
            .establishConnection(false, Timeout(10, TimeUnit.SECONDS))
            .map { rxBleClient -> Optional.of(rxBleClient) }
            .startWith(Optional.empty())

        val operation2 = device.observeConnectionStateChanges()

        val disposable = Observable.combineLatest(
            operation1,
            operation2,
            BiFunction<Optional<RxBleConnection>, RxBleConnection.RxBleConnectionState, ConnectionStatus>
            { connectionOptional, state ->
                mapResult(state, device, connectionOptional, isHeartRateServiceRequired)
            }
        )
            .onErrorReturn { throwable -> ConnectionStatus.Failed(device, throwable) }
            .subscribeOn(executionThreads.bleScheduler)
            .doOnNext(callback)
            .subscribe()
        return DisposableCancellable(disposable)
    }

    private fun mapResult(
        state: RxBleConnection.RxBleConnectionState,
        device: RxBleDevice,
        connectionOptional: Optional<RxBleConnection>,
        isHeartRateServiceRequired: Boolean
    ): ConnectionStatus {
        return when (state) {
            RxBleConnection.RxBleConnectionState.CONNECTING -> {
                messageRepo.log("Connecting to ${device.macAddress}")
                ConnectionStatus.Connecting(device)
            }
            RxBleConnection.RxBleConnectionState.CONNECTED ->
                if (connectionOptional.isPresent()) {
                    val connection = connectionOptional.get()
                    if (isHeartRateServiceRequired) {
                        messageRepo.log("Connected to ${device.macAddress} and checking for heart rate service")
                        checkForHeartRateService(connection, device)
                    } else {
                        messageRepo.log("Connecting to ${device.macAddress} blindly")
                        ConnectionStatus.Connected(device, connection)
                    }
                } else {
                    messageRepo.log("Connecting to ${device.macAddress} but connection not received")
                    ConnectionStatus.Connecting(device)
                }
            RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                messageRepo.log("Disconnected from ${device.macAddress}")
                ConnectionStatus.Disconnected(device)
            }
            RxBleConnection.RxBleConnectionState.DISCONNECTING -> {
                messageRepo.log("Disconnecting from ${device.macAddress}")
                ConnectionStatus.Disconnecting(device)
            }
        }
    }

    private fun checkForHeartRateService(
        connection: RxBleConnection,
        device: RxBleDevice
    ): ConnectionStatus {
        val heartRateService = connection
            .discoverServices(5, TimeUnit.SECONDS)
            .flatMap { services ->
                services.getService(UUID.fromString(miServices.heartRateService.service))
            }
            .map { service -> Optional.of(service) }
            .onErrorReturn { Optional.empty() }
            .subscribeOn(executionThreads.bleScheduler)
            .blockingGet()
        return if (heartRateService.isPresent()) {
            messageRepo.log("Connecting to ${device.macAddress} and found heart rate service")
            ConnectionStatus.Connected(device, connection)
        } else {
            messageRepo.log("Connecting to ${device.macAddress} but missing heart rate service")
            ConnectionStatus.ConnectedNoHeartRate(device, connection)
        }
    }
}