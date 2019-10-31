package com.michaelfotiadis.heartbeat.core.permission

import android.Manifest
import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener

class PermissionsHandler {

    fun askForLocationPermission(activity: Activity, onGranted: () -> Unit, onDenied: () -> Unit) {
        val dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
            .withContext(activity)
            .withTitle("Location Permission")
            .withMessage("Location Permission is needed in order to scan for nearby Bluetooth Devices")
            .withButtonText(android.R.string.ok)
            .build()

        val resultPermissionListener = object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                onGranted.invoke()
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                onDenied.invoke()
            }

        }

        Dexter.withActivity(activity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(
                CompositePermissionListener(
                    dialogPermissionListener,
                    resultPermissionListener
                )
            )
            .check()
    }

}