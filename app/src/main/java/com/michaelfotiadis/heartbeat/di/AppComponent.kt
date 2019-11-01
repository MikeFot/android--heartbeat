package com.michaelfotiadis.heartbeat.di

import android.app.Application
import com.michaelfotiadis.heartbeat.HeartBeatApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityBuilderModule::class,
        ServiceBuilderModule::class,
        AppModule::class,
        BluetoothModule::class
    ]
)
interface AppComponent : AndroidInjector<HeartBeatApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}
