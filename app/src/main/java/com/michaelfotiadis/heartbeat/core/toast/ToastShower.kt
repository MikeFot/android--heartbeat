package com.michaelfotiadis.heartbeat.core.toast

import android.content.Context
import androidx.annotation.StringRes
import es.dmoral.toasty.Toasty

class ToastShower {

    fun initialize() {
        Toasty.Config.getInstance()
            .tintIcon(true)
            .apply()
    }

    fun info(context: Context, message: String) {
        Toasty.info(context, message).show()
    }

    fun info(context: Context, @StringRes messageResId: Int) {
        Toasty.info(context, messageResId).show()
    }

}