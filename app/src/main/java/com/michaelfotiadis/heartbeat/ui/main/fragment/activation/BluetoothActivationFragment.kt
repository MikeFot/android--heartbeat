package com.michaelfotiadis.heartbeat.ui.main.fragment.activation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.core.toast.ToastShower
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import kotlinx.android.synthetic.main.bluetooth_activation_fragment.*
import javax.inject.Inject

internal class BluetoothActivationFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: BluetoothActivationViewModelFactory
    @Inject
    lateinit var toastShower: ToastShower

    private val viewModel: BluetoothActivationViewModel by lazy {
        ViewModelProviders.of(this, factory).get(BluetoothActivationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bluetooth_activation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.actionLiveData.observe(viewLifecycleOwner, Observer(this::processAction))

        bluetooth_activate_button.setOnClickListener {
            viewModel.enableBluetooth()
        }
        viewModel.checkStatus()
    }

    private fun processAction(action: Action?) {
        when (action) {
            Action.MOVE_TO_NEXT -> moveToNext()
            Action.BLUETOOTH_UNAVAILABLE -> bluetooth_activate_button.isEnabled = true
            null -> bluetooth_activate_button.isEnabled = false
        }
    }

    private fun moveToNext() {
        navController.navigate(
            BluetoothActivationFragmentDirections.actionBluetoothActivationFragmentToBondedDevicesFragment()
        )
    }
}
