package com.gdsc.bingo.ui.pinpoint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.ActivityPinPointBinding

class PinPointActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPinPointBinding.inflate(layoutInflater)
    }
    private lateinit var navController : NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupNavController()
        setupBackButton()
    }

    private fun setupNavController() {
        val navHostController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_pin_point) as NavHostFragment
        navController = navHostController.navController
    }


    private fun setupBackButton() {
        binding.pinpointHeaderButtonBack.setOnClickListener {
            val previousBackStackEntry = navController.previousBackStackEntry
            if (previousBackStackEntry == null) {
                finish()
            } else {
                navController.navigateUp()
            }
        }
    }


}