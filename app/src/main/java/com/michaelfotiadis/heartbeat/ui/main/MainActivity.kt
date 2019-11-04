package com.michaelfotiadis.heartbeat.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.service.BluetoothService
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : DaggerAppCompatActivity(), ServiceConnection {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    @Inject
    lateinit var serviceDispatcher: BluetoothServiceDispatcher

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private var service: BluetoothService? = null
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        serviceDispatcher.startService()

        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.bluetoothActivationFragment,
            R.id.bondedDevicesFragment
        ).build()

        main_toolbar.also { toolbar ->
            setSupportActionBar(toolbar)
            toolbar.setupWithNavController(navController, appBarConfiguration)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceDispatcher.stopService()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        if (service == null) {
            // Bind to the service
            serviceDispatcher.createIntent().also { intent ->
                bindService(intent, this, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (service != null) {
            unbindService(this)
            service = null
        }
    }

    override fun onServiceDisconnected(className: ComponentName?) {
        service = null
    }

    override fun onServiceConnected(className: ComponentName?, iBinder: IBinder?) {
        service = (iBinder as BluetoothService.LocalBinder).getService()
    }
}
