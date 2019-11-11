package com.michaelfotiadis.heartbeat.ui.main.fragment.pair

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.michaelfotiadis.heartbeat.R

class ViewBinder(view: View) {

    private val connectImage: ImageView = view.findViewById(R.id.connect_image)
    private val connectProgress: ProgressBar = view.findViewById(R.id.connect_progress)
    private val statusText: TextView = view.findViewById(R.id.connect_text)
    private val retryButton: Button = view.findViewById(R.id.connect_retry_button)
    private val cancelButton: Button = view.findViewById(R.id.connect_cancel_button)

    var callbacks: Callbacks? = null

    interface Callbacks {
        fun onRetry()
        fun onCancel()
    }

    init {
        retryButton.setOnClickListener {
            callbacks?.onRetry()
        }
        cancelButton.setOnClickListener {
            callbacks?.onCancel()
        }
    }

    fun showIdle() {
        connectImage.setImageResource(R.drawable.ic_bluetooth_white_24dp)
        connectProgress.visibility = View.INVISIBLE
        statusText.text = "Waiting for device..."
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showConnecting() {
        connectProgress.visibility = View.VISIBLE
        connectImage.setImageResource(R.drawable.ic_bluetooth_searching_white_24dp)
        statusText.text = "Connecting..."
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showConnected() {
        connectProgress.visibility = View.VISIBLE
        connectImage.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp)
        statusText.text = "Connected successfully. Discovering services..."
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showServicesDiscovered() {
        connectProgress.visibility = View.VISIBLE
        connectImage.setImageResource(R.drawable.ic_vibration_white_24dp)
        statusText.text = "Your device should vibrate. Please tap the button on it."
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showAuthorising() {
        connectProgress.visibility = View.VISIBLE
        connectImage.setImageResource(R.drawable.ic_vpn_key_white_24dp)
        statusText.text = "Authorising..."
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showAuthorisedOne() {
        connectProgress.visibility = View.VISIBLE
        connectImage.setImageResource(R.drawable.ic_vpn_key_white_24dp)
        statusText.text = "Authorising... (step 1/3)"
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showAuthorisedTwo() {
        connectProgress.visibility = View.VISIBLE
        connectImage.setImageResource(R.drawable.ic_vpn_key_white_24dp)
        statusText.text = "Authorising... (step 2/3)"
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showAuthorisedDone() {
        connectProgress.visibility = View.INVISIBLE
        connectImage.setImageResource(R.drawable.ic_verified_user_white_24dp)
        statusText.text = "Authorised successfully."
        retryButton.visibility = View.INVISIBLE
        retryButton.isEnabled = false
    }

    fun showAuthorisedFailed() {
        connectProgress.visibility = View.INVISIBLE
        connectImage.setImageResource(R.drawable.ic_cancel_red_24dp)
        statusText.text = "Failed to authorise."
        retryButton.visibility = View.VISIBLE
        retryButton.isEnabled = true
    }

    fun showDisconnected() {
        connectProgress.visibility = View.INVISIBLE
        connectImage.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp)
        statusText.text = "Device disconnected"
        retryButton.visibility = View.VISIBLE
        retryButton.isEnabled = true
    }


}
