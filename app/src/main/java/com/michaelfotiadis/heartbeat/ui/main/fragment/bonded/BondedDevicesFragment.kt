package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.adapter.BluetoothBondedDevicesAdapter
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.viewmodel.Action
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.viewmodel.BondedDevicesViewModel
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.viewmodel.BondedDevicesViewModelFactory
import kotlinx.android.synthetic.main.fragment_bonded_devices.*
import javax.inject.Inject

internal class BondedDevicesFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: BondedDevicesViewModelFactory

    private val viewModel by viewModels<BondedDevicesViewModel>({ this }, { factory })

    private var adapter: BluetoothBondedDevicesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bonded_devices, container, false)
    }

    override fun onDestroyView() {
        adapter?.listener = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        setupObservers()
        viewModel.refreshBondedDevices()
    }

    override fun onResume() {
        super.onResume()
        viewModel.disconnectDevice()
    }

    private fun bindViews() {
        adapter = BluetoothBondedDevicesAdapter().apply {
            listener = { device ->
                viewModel.onDeviceSelected(device)
            }
        }
        bonded_devices_recycler_view.adapter = adapter
        bonded_devices_recycler_view.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        bonded_devices_missing_button.setOnClickListener {
            viewModel.onMissingDeviceClicked()
        }
    }

    private fun setupObservers() {
        viewModel.devicesLiveData.observe(
            viewLifecycleOwner,
            Observer { list -> adapter?.submitList(list) })
        viewModel.actionLiveData.observe(viewLifecycleOwner, Observer(this::onActionReceived))
    }

    private fun onActionReceived(action: Action?) {
        when (action) {
            Action.MoveToLocationPermission -> navigateToLocationPermission()
            is Action.ConnectToDevice -> navigateToPairToDevice(action.uiBondedDevice)
            null -> {
                // NOOP
            }
        }
    }

    private fun navigateToLocationPermission() {
        navController.navigate(
            BondedDevicesFragmentDirections.actionBondedDevicesFragmentToLocationPermissionFragment()
        )
    }

    private fun navigateToPairToDevice(uiBondedDevice: UiBondedDevice) {
        navController.navigate(
            BondedDevicesFragmentDirections.actionBondedDevicesFragmentToPairDeviceFragment(
                macAddress = uiBondedDevice.address
            )
        )
    }
}
