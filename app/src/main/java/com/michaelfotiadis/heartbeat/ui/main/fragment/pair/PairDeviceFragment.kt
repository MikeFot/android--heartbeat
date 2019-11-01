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
import javax.inject.Inject

internal class PairDeviceFragment : BaseNavFragment() {


    @Inject
    lateinit var factory: PairDeviceViewModelFactory
    @Inject
    lateinit var appLogger: AppLogger

    val args: PairDeviceFragmentArgs by navArgs()

    private val viewModel: PairDeviceViewModel by lazy {
        ViewModelProviders.of(this, factory).get(PairDeviceViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pair_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.connectionResultLiveData.observe(viewLifecycleOwner, Observer { result ->
            appLogger.get().d("Connection result $result")
        })
        viewModel.connect(args.macAddress)
    }
}
