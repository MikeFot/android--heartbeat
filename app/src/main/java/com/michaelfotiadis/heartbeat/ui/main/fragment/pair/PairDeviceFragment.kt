package com.michaelfotiadis.heartbeat.ui.main.fragment.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    private val viewModel by viewModels<PairDeviceViewModel>({ this }, { factory })

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
            Action.IDLE -> binder.showIdle()
            Action.CONNECTING -> binder.showConnecting()
            Action.CONNECTED -> binder.showConnected()
            Action.DISCONNECTED -> binder.showDisconnected()
            Action.SERVICES_DISCOVERED -> binder.showServicesDiscovered()
            Action.AUTH_NOTIFIED -> binder.showAuthorising()
            Action.AUTH_STEP_ONE -> binder.showAuthorisedOne()
            Action.AUTH_STEP_TWO -> binder.showAuthorisedTwo()
            Action.AUTH_DONE -> binder.showAuthorisedDone()
            Action.AUTH_FAILED -> binder.showAuthorisedFailed()
        }
    }
}
