package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.adapter.BluetoothDevicesAdapter
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDeviceMapper
import kotlinx.android.synthetic.main.fragment_bonded_devices.*
import javax.inject.Inject

internal class BondedDevicesFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: BondedDevicesViewModelFactory
    @Inject
    lateinit var uiBondedDeviceMapper: UiBondedDeviceMapper

    private val viewModel: BondedDevicesViewModel by lazy {
        ViewModelProviders.of(this, factory).get(BondedDevicesViewModel::class.java)
    }

    private lateinit var adapter: BluetoothDevicesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bonded_devices, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        setupObservers()
        viewModel.refreshBondedDevices()
    }

    private fun bindViews() {
        adapter = BluetoothDevicesAdapter()
        adapter.listener = viewModel::onDeviceSelected
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
        viewModel.devicesLiveData.observe(viewLifecycleOwner, Observer(adapter::submitList))
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
