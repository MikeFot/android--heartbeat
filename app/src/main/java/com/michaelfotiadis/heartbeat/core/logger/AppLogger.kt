package com.michaelfotiadis.heartbeat.core.logger

import com.michaelfotiadis.heartbeat.core.features.FeatureFlagProvider
import org.jetbrains.annotations.NotNull
import timber.log.Timber

private const val TAG = "APP"

class AppLogger(private val featureFlagProvider: FeatureFlagProvider) {

    fun activate() {
        if (featureFlagProvider.isDebugEnabled) {
            Timber.plant(Timber.DebugTree())
            get().d("Timber initialised")
        } else {
            Timber.e("You should not be seeing this!")
        }
    }

    fun get(): @NotNull Timber.Tree {
        return get(TAG)
    }

    fun get(tag: String): @NotNull Timber.Tree {
        return Timber.tag(tag)
    }
}
