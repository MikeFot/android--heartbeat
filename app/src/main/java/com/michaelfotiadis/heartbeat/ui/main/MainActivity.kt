package com.michaelfotiadis.heartbeat.ui.main

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.michaelfotiadis.heartbeat.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*


private const val KEY_IS_ROOT = "isRoot"

class MainActivity : DaggerAppCompatActivity() {

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        // set up with custom root destinations, on which the back button will be hidden
        appBarConfiguration = AppBarConfiguration.Builder(findRootDestinationIds()).build()
        main_toolbar.also { toolbar ->
            setSupportActionBar(toolbar)
            toolbar.setupWithNavController(
                navController,
                appBarConfiguration
            )
        }
    }

    /**
     * Iterate through the nav graph and find all destinations that have the argument [KEY_IS_ROOT] set as true
     * @return HashSet<Int> of all destination IDs
     */
    private fun findRootDestinationIds(): Set<Int> {
        return navController.graph
            .filter { destination -> destination.arguments[KEY_IS_ROOT]?.defaultValue == true }
            .map { destination -> destination.id }.toSet()
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }
}
