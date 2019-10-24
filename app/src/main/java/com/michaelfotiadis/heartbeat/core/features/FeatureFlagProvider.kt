package com.michaelfotiadis.heartbeat.core.features

import com.michaelfotiadis.heartbeat.BuildConfig

class FeatureFlagProvider {
    val isDebugEnabled = BuildConfig.DEBUG
}