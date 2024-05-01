package com.gdsc.bingo.ui.pinpoint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.ActivityPinPointBinding
import com.gdsc.bingo.ui.CustomSystemTweak

class PinPointActivity : AppCompatActivity() {

    val args : PinPointActivityArgs by navArgs()

    val binding by lazy {
        ActivityPinPointBinding.inflate(layoutInflater)
    }
    private lateinit var navController : NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        CustomSystemTweak(this)
            .statusBarTweak()
            .customNavigationBarColorSet(com.google.android.material.R.attr.colorSurfaceContainer)

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