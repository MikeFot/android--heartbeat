package com.michaelfotiadis.heartbeat.ui.main.fragment.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceInfo
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.info.viewmodel.DeviceInfoViewModel
import com.michaelfotiadis.heartbeat.ui.main.fragment.info.viewmodel.DeviceInfoViewModelFactory
import kotlinx.android.synthetic.main.fragment_device_info.*
import javax.inject.Inject

internal class DeviceInfoFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: DeviceInfoViewModelFactory

    private val viewModel by viewModels<DeviceInfoViewModel>({ this }, { factory })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.deviceInfoLiveData.observe(
            viewLifecycleOwner,
            Observer(this::onDeviceInfoChanged)
        )
        viewModel.disconnectLiveData.observe(
            viewLifecycleOwner,
            Observer { moveToStartScreen() }
        )

        info_disconnect_button.setOnClickListener {
            viewModel.disconnectFromDevice()
        }

        viewModel.refreshDeviceInfo()
    }

    private fun onDeviceInfoChanged(deviceInfo: DeviceInfo?) {

        if (deviceInfo != null) {
            deviceInfo.name?.let { name ->
                info_name_body.setAndStop(name)
            }
            deviceInfo.address?.let { address ->
                info_mac_body.setAndStop(address)
            }
            deviceInfo.batteryLevel?.let { level ->
                info_battery_body.setAndStop("$level%")
            }
            deviceInfo.serialNumber?.let { serialNumber ->
                info_serial_number_body.setAndStop(serialNumber)
            }
            deviceInfo.softwareRevision?.let { rev ->
                info_software_rev_body.setAndStop(rev)
            }
            deviceInfo.hardwareRevision?.let { rev ->
                info_hardware_rev_body.setAndStop(rev)
            }
        }
    }

    private fun moveToStartScreen() {
        navController.navigate(
            DeviceInfoFragmentDirections.actionDeviceInfoFragmentToBluetoothActivationFragment()
        )
    }

}
