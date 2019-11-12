package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice

class BluetoothBondedDevicesAdapter :
    ListAdapter<UiBondedDevice, BluetoothBondedDevicesAdapter.ViewHolder>(
        DiffCallback()
    ) {

    var listener: ((UiBondedDevice) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val device = getItem(position)
        holder.run {
            titleTextView.text = device.address
            descriptionTextView.text = device.name
            typeTextView.text = device.deviceType.toString()
            bondedTextView.text = device.bondedStatus.toString()
            rssiTextView.visibility = View.GONE
            holder.itemView.setOnClickListener { listener?.invoke(device) }
        }
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val titleTextView: TextView = rootView.findViewById(R.id.device_title)
        val descriptionTextView: TextView = rootView.findViewById(R.id.device_content)
        val typeTextView: TextView = rootView.findViewById(R.id.device_type)
        val bondedTextView: TextView = rootView.findViewById(R.id.device_bonded)
        val rssiTextView: TextView = rootView.findViewById(R.id.device_rssi)
    }

    class DiffCallback : DiffUtil.ItemCallback<UiBondedDevice>() {
        override fun areItemsTheSame(oldItem: UiBondedDevice, newItem: UiBondedDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: UiBondedDevice, newItem: UiBondedDevice): Boolean {
            return oldItem == newItem
        }
    }
}
