package com.michaelfotiadis.heartbeat.ui.main.fragment.activation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothWrapper
import com.michaelfotiadis.heartbeat.core.toast.ToastShower
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import kotlinx.android.synthetic.main.bluetooth_activation_fragment.*
import javax.inject.Inject

internal class BluetoothActivationFragment : BaseNavFragment() {

    @Inject
    lateinit var bluetoothWrapper: BluetoothWrapper
    @Inject
    lateinit var toastShower: ToastShower

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bluetooth_activation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetooth_activate_button.setOnClickListener {
            if (bluetoothWrapper.isBluetoothEnabled()) {
                moveToNext()
            } else {
                bluetoothWrapper.askToEnableBluetooth(this)
            }
        }

        checkForBluetooth()
    }

    private fun checkForBluetooth() {
        if (bluetoothWrapper.isBluetoothEnabled()) {
            moveToNext()
        }
    }

    private fun moveToNext() {
        navController.navigate(
            BluetoothActivationFragmentDirections.actionBluetoothActivationFragmentToBondedDevicesFragment()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (bluetoothWrapper.isBluetoothEnabled()) {
            moveToNext()
        } else {
            toastShower.info(requireContext(), "Cannot proceed without Bluetooth")
        }
    }

}
