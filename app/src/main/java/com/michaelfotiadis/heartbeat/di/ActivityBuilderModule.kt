package com.michaelfotiadis.heartbeat.di

import com.michaelfotiadis.heartbeat.ui.main.MainActivity
import com.michaelfotiadis.heartbeat.ui.main.di.MainFragmentBuilderModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [MainFragmentBuilderModule::class])
    abstract fun mainActivity(): MainActivity
}
