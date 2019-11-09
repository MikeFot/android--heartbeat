package com.michaelfotiadis.heartbeat.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import com.michaelfotiadis.heartbeat.core.storage.Storage
import com.michaelfotiadis.heartbeat.service.BluetoothService
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), ServiceConnection {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    @Inject
    lateinit var serviceDispatcher: BluetoothServiceDispatcher
    @Inject
    lateinit var appLogger: AppLogger
    @Inject
    lateinit var storage: Storage

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private var service: BluetoothService? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var isServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(storage.getActiveThemeAttributeStyle())
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

        storage.getLiveThemeTrigger().observe(this, Observer(::handleThemeTrigger))
    }

    private fun handleThemeTrigger(trigger: Boolean?) {
        trigger?.let {
            if (it) {
                recreate()
            }
        }
    }

    override fun onDestroy() {
        serviceDispatcher.stopService()
        service = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        // Bind to the service
        serviceDispatcher.createIntent().also { intent ->
            isServiceBound = bindService(intent, this, Context.BIND_NOT_FOREGROUND)
            appLogger.get().d("Binding Service '$isServiceBound'")
        }
    }

    override fun onPause() {
        super.onPause()
        if (isServiceBound) {
            // Unbind from the service
            appLogger.get().d("Unbinding Service")
            unbindService(this)
        }
    }

    override fun onServiceDisconnected(className: ComponentName?) {
        appLogger.get().d("Service Disconnected")
        service = null
    }

    override fun onServiceConnected(className: ComponentName?, iBinder: IBinder?) {
        appLogger.get().d("Service Connected")
        service = (iBinder as BluetoothService.LocalBinder).getService()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_theme -> {
                storage.goToNextTheme()
                return true
            }
        }
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
}
