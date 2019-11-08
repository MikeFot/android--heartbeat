package com.michaelfotiadis.heartbeat.ui.main.fragment.pair

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.ViewFlipper
import com.michaelfotiadis.heartbeat.R

class ViewBinder(view: View) {
    private companion object {
        private const val INDEX_PROGRESS = 0
        private const val INDEX_CONNECTED = 1
        private const val INDEX_DISCONNECTED = 2
        private const val INDEX_FAILED = 3
        private const val INDEX_HEART_SUCCESS = 4
        private const val INDEX_HEART_UPDATED = 5
    }

    private val viewFlipper: ViewFlipper = view.findViewById(R.id.pair_view_flipper)
    private val failedRetryButton: Button = view.findViewById(R.id.pair_failed_retry)
    private val failedCancelButton: Button = view.findViewById(R.id.pair_failed_cancel)
    private val disconnectedRetryButton: Button = view.findViewById(R.id.pair_disconnected_retry)
    private val disconnectedCancelButton: Button = view.findViewById(R.id.pair_disconnected_cancel)
    private val failedDescriptionText: TextView = view.findViewById(R.id.pair_failed_description)
    private val heartRateValueText: TextView = view.findViewById(R.id.pair_heart_updated_value)

    var callbacks: Callbacks? = null

    interface Callbacks {
        fun onRetry()
        fun onCancel()
    }

    init {
        viewFlipper.apply {
            displayedChild = INDEX_PROGRESS
            inAnimation = AnimationUtils.loadAnimation(view.context, R.anim.enter_from_right)
            outAnimation = AnimationUtils.loadAnimation(view.context, R.anim.exit_to_left)
        }

        failedRetryButton.setOnClickListener {
            callbacks?.onRetry()
        }
        disconnectedRetryButton.setOnClickListener {
            callbacks?.onRetry()
        }
        failedCancelButton.setOnClickListener {
            callbacks?.onCancel()
        }
        disconnectedCancelButton.setOnClickListener {
            callbacks?.onCancel()
        }
    }

    fun showProgress() {
        viewFlipper.displayedChild = INDEX_PROGRESS
    }

    fun showConnected() {
        viewFlipper.displayedChild = INDEX_CONNECTED
    }

    fun showDisconnected() {
        viewFlipper.displayedChild = INDEX_DISCONNECTED
    }

    fun showFailed(message: String) {
        viewFlipper.displayedChild = INDEX_FAILED
        failedDescriptionText.text = message
    }

    fun showHeartRateSuccess() {
        viewFlipper.displayedChild = INDEX_HEART_SUCCESS
    }

    fun showHeartRateUpdated(heartRate: Int) {
        if (viewFlipper.displayedChild != INDEX_HEART_UPDATED) {
            viewFlipper.displayedChild = INDEX_HEART_UPDATED
        }
        heartRateValueText.text = heartRate.toString()
    }
}
