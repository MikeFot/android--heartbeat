package com.michaelfotiadis.heartbeat.core.storage

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.chibatching.kotpref.KotprefModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.core.livedata.SingleLiveEvent

class LocalStorage(context: Context) : KotprefModel(context), Storage {
    private val themeMap = mapOf(
        ThemeVariant.PURPLE.name to R.style.Theme_App_Purple,
        ThemeVariant.BLUE.name to R.style.Theme_App_Blue,
        ThemeVariant.PINK.name to R.style.Theme_App_Pink
    )

    private var themeActive by stringPref(
        key = "theme_active",
        default = ThemeVariant.PURPLE.name
    )

    private val themeTrigger = SingleLiveEvent<Boolean>()

    override fun getLiveThemeTrigger(): MutableLiveData<Boolean> {
        return themeTrigger
    }

    override fun getActiveTheme(): ThemeVariant {
        return ThemeVariant.valueOf(themeActive)
    }

    override fun changeTheme(themeVariant: ThemeVariant) {
        themeActive = themeVariant.name
        themeTrigger.value = true
    }

    override fun goToNextTheme() {
        when (getActiveTheme()) {
            ThemeVariant.BLUE -> changeTheme(ThemeVariant.PURPLE)
            ThemeVariant.PURPLE -> changeTheme(ThemeVariant.PINK)
            ThemeVariant.PINK -> changeTheme(ThemeVariant.BLUE)
        }
    }

    override fun getActiveThemeAttributeStyle(): Int {
        return themeMap.getValue(getActiveTheme().name)
    }

}

/**
 * Helps to get Map, List, Set or other generic type from Json using Gson.
 */
inline fun <reified T : Any> Gson.fromJsonToGeneric(json: String): T {
    val type = object : TypeToken<T>() {}.type
    return fromJson(json, type)
}