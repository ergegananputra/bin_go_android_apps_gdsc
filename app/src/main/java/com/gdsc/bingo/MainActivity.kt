package com.gdsc.bingo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gdsc.bingo.databinding.ActivityMainBinding
import com.gdsc.bingo.ui.artikel.ArtikelFragment
import com.gdsc.bingo.ui.points_history.PointsHistoryFragment
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
            is PointsHistoryFragment -> bottomViewGone()
            is ProfilDetailFragment -> bottomViewGone()
            is ArtikelFragment -> bottomViewGone()
            else -> bottomViewVisible()
        }
    }

    private fun bottomViewGone() {
        binding.mainBottomNavigation.visibility = View.GONE
    }

    private fun bottomViewVisible() {
        binding.mainBottomNavigation.visibility = View.VISIBLE
    }

}