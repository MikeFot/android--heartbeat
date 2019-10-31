package com.michaelfotiadis.heartbeat.ui.main.fragment.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.core.permission.PermissionsHandler
import com.michaelfotiadis.heartbeat.core.toast.ToastShower
import com.michaelfotiadis.heartbeat.ui.base.BaseNavFragment
import javax.inject.Inject

internal class LocationPermissionFragment : BaseNavFragment() {

    companion object {
        fun newInstance() =
            LocationPermissionFragment()
    }

    @Inject
    lateinit var permissionsHandler: PermissionsHandler
    @Inject
    lateinit var toastShower: ToastShower

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionsHandler.askForLocationPermission(requireActivity(),
            onGranted = { moveToNext() },
            onDenied = { endFlow() }
        )
    }

    private fun moveToNext() {
        toastShower.info(requireContext(), "Move to Next")
    }

    private fun endFlow() {
        toastShower.info(requireContext(), "End Flow")
    }

}
