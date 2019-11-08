package com.michaelfotiadis.heartbeat.ui.main.fragment.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel.Action
import com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel.PairDeviceViewModel
import com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel.PairDeviceViewModelFactory
import javax.inject.Inject

internal class PairDeviceFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: PairDeviceViewModelFactory
    @Inject
    lateinit var appLogger: AppLogger

    val args: PairDeviceFragmentArgs by navArgs()

    private lateinit var binder: ViewBinder

    private val viewModel: PairDeviceViewModel by lazy {
        ViewModelProviders.of(this, factory).get(PairDeviceViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pair_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binder = ViewBinder(view)
        binder.callbacks = object : ViewBinder.Callbacks {
            override fun onRetry() {
                attemptConnection()
            }

            override fun onCancel() {
                abortFlow()
            }
        }
        viewModel.connectionResultLiveData.observe(
            viewLifecycleOwner,
            Observer(this::processResult)
        )
        attemptConnection()
    }

    private fun abortFlow() {
        navController.navigate(
            PairDeviceFragmentDirections.actionPairDeviceFragmentToBondedDevicesFragment()
        )
    }

    private fun attemptConnection() {
        viewModel.connect(args.macAddress)
    }

    private fun processResult(action: Action) {
        appLogger.get().d("Action $action")
        when (action) {
            Action.ConnectionIdle -> {
                // NOOP
            }
            Action.ConnectionStarted -> binder.showProgress()
            is Action.ConnectionFailed -> binder.showFailed(action.message)
            is Action.ConnectionDisconnected -> binder.showDisconnected()
            is Action.ConnectionConnected -> {
                binder.showConnected()
                viewModel.checkSerial()
            }
            Action.HeartRateIdle -> {
                // NOOP
            }
            Action.HeartRateSuccess -> binder.showHeartRateSuccess()
            is Action.HeartRateFailed -> binder.showFailed(action.message)
            is Action.HeartRateUpdated -> binder.showHeartRateUpdated(action.heartRate)
        }
    }
}
