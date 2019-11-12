package com.michaelfotiadis.heartbeat.di

import android.app.Application
import android.content.Context
import com.clj.fastble.BleManager
import com.clj.fastble.scan.BleScanRuleConfig
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothHandler
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.factory.BluetoothInteractorFactory
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
internal class BluetoothModule {

    @Provides
    @Singleton
    fun providesMessageRepo(executionThreads: ExecutionThreads): MessageRepo {
        return MessageRepo(executionThreads)
    }

    @Provides
    @Singleton
    fun providesBleManager(context: Context): BleManager {
        return BleManager.getInstance().apply {
            init(context.applicationContext as Application)
            enableLog(true)
            initScanRule(
                BleScanRuleConfig.Builder()
                    .setScanTimeOut(TimeUnit.SECONDS.toMillis(10))
                    .build()
            )
        }
    }

    @Provides
    fun provideBluetoothInteractorFactory(
        bleManager: BleManager,
        miServices: MiServices,
        messageRepo: MessageRepo,
        executionThreads: ExecutionThreads
    ): BluetoothInteractorFactory {
        return BluetoothInteractorFactory(
            bleManager,
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Provides
    @Singleton
    fun providesBluetoothWrapper(
        context: Context,
        bluetoothRepo: BluetoothRepo,
        messageRepo: MessageRepo,
        bluetoothInteractorFactory: BluetoothInteractorFactory
    ): BluetoothHandler {
        return BluetoothHandler(
            context,
            bluetoothRepo,
            messageRepo,
            bluetoothInteractorFactory
        )
    }

    @Provides
    @Singleton
    fun providesBluetoothStatusProvider(executionThreads: ExecutionThreads): BluetoothRepo {
        return BluetoothRepo(executionThreads)
    }
}