package com.michaelfotiadis.heartbeat.core.storage

import androidx.lifecycle.MutableLiveData

interface Storage {

    fun getLiveThemeTrigger(): MutableLiveData<Boolean>

    fun changeTheme(themeVariant: ThemeVariant)

    fun getActiveTheme(): ThemeVariant

    fun getActiveThemeAttributeStyle(): Int

    fun goToNextTheme()
}