package com.michaelfotiadis.heartbeat.ui.base

import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment

internal abstract class BaseNavFragment : DaggerFragment() {

    val navController: NavController by lazy {
        findNavController()
    }
}
