package com.gdsc.bingo

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gdsc.bingo.databinding.ActivityMainBinding
import com.gdsc.bingo.ui.points_history.PointsHistoryFragment
import com.gdsc.bingo.ui.profil.ProfilFragment
import com.gdsc.bingo.ui.profil.detail.ProfilDetailFragment

class MainActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        with(binding) {
            val navHostController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val navController = navHostController.navController
            mainBottomNavigation.setupWithNavController(navController)
        }
    }

    fun setBottomNavigationVisibility(fragment: Fragment) {
        when (fragment) {
            is PointsHistoryFragment -> {
                binding.mainBottomNavigation.visibility = View.GONE
            }
            is ProfilDetailFragment -> {
                binding.mainBottomNavigation.visibility = View.GONE
            }
            else -> {
                binding.mainBottomNavigation.visibility = View.VISIBLE
            }
        }
    }

}