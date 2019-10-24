package com.michaelfotiadis.heartbeat.ui.main.di

import com.michaelfotiadis.heartbeat.ui.main.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class MainFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun mainFragment(): MainFragment
}
