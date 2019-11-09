package com.michaelfotiadis.heartbeat.ui.main.fragment.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.scan.adapter.BluetoothScannedDevicesAdapter
import com.michaelfotiadis.heartbeat.ui.main.fragment.scan.viewmodel.Action
import com.michaelfotiadis.heartbeat.ui.main.fragment.scan.viewmodel.ScanDevicesViewModel
import com.michaelfotiadis.heartbeat.ui.main.fragment.scan.viewmodel.ScanDevicesViewModelFactory
import kotlinx.android.synthetic.main.fragment_scan_devices.*
import javax.inject.Inject

internal class ScanDevicesFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: ScanDevicesViewModelFactory

    private val viewModel by viewModels<ScanDevicesViewModel>({ this }, { factory })

    private lateinit var adapter: BluetoothScannedDevicesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        setupObservers()
        viewModel.scanForDevices()
    }

    private fun bindViews() {
        adapter = BluetoothScannedDevicesAdapter()
        adapter.listener = viewModel::onDeviceSelected
        scan_devices_recycler_view.adapter = adapter
        scan_devices_recycler_view.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun setupObservers() {
        viewModel.devicesLiveData.observe(viewLifecycleOwner, Observer(adapter::submitList))
        viewModel.actionLiveData.observe(viewLifecycleOwner, Observer(this::onActionChanged))
    }

    private fun onActionChanged(action: Action?) {
        when (action) {
            Action.SCANNING -> {
                scan_devices_info_text.text = "Scanning for devices..."
                scan_devices_progress.visibility = View.VISIBLE
            }
            Action.FINISHED -> {
                scan_devices_info_text.text = "Finished Scanning."
                scan_devices_progress.visibility = View.INVISIBLE
            }
            null -> {
                // NOOP
            }
        }
    }
}
