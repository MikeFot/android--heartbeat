package com.michaelfotiadis.heartbeat.ui.main.fragment.connected

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.connected.viewmodel.Action
import com.michaelfotiadis.heartbeat.ui.main.fragment.connected.viewmodel.DashboardViewModel
import com.michaelfotiadis.heartbeat.ui.main.fragment.connected.viewmodel.DashboardViewModelFactory
import kotlinx.android.synthetic.main.includable_card_device_info.*
import kotlinx.android.synthetic.main.includable_card_heart_continuous.*
import kotlinx.android.synthetic.main.includable_card_heart_single.*
import kotlinx.android.synthetic.main.includable_card_profile.*
import javax.inject.Inject

internal class DashboardFragment : BaseNavFragment() {

    @Inject
    lateinit var factory: DashboardViewModelFactory

    private val viewModel by viewModels<DashboardViewModel>(
        { this },
        { factory })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboard_card_heart_single.setOnClickListener {
            viewModel.onSingleHeartRateClicked()
        }
        dashboard_card_heart_continuous.setOnClickListener {
            viewModel.onContinuousHeartRateClicked()
        }
        dashboard_card_profile.setOnClickListener {
            viewModel.onProfileClicked()
        }
        dashboard_card_device.setOnClickListener {
            viewModel.onDeviceInfoClicked()
        }

        viewModel.actionLiveData.observe(viewLifecycleOwner, Observer { action ->
            when (action) {
                Action.SHOW_HEART_RATE_SINGLE -> showSingleHeartRate()
                Action.SHOW_HEART_RATE_CONTINUOUS -> showContinuousHeartRate()
                Action.MY_PROFILE -> showProfile()
                Action.SHOW_DEVICE_INFO -> showDeviceInfo()
                else -> {
                    // NOOP
                }
            }

        })
    }

    private fun showSingleHeartRate() {
    }

    private fun showContinuousHeartRate() {
    }

    private fun showProfile() {
    }

    private fun showDeviceInfo() {
        navController.navigate(DashboardFragmentDirections.actionConnectedFragmentToDeviceInfoFragment())
    }
}