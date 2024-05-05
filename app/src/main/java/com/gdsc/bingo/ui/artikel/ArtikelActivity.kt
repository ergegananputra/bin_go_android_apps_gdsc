package com.gdsc.bingo.ui.artikel

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.gdsc.bingo.databinding.ActivityArtikelBinding

class ArtikelActivity : AppCompatActivity() {
    val args : ArtikelActivityArgs by navArgs()
    private lateinit var artikelNavController : NavController

    private val binding by lazy {
        ActivityArtikelBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragmentArtikel.id) as NavHostFragment
        artikelNavController = navHostFragment.navController

        setSupportActionBar(binding.artikelToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        val previousBackStackEntry = artikelNavController.previousBackStackEntry
        Log.d("ArtikelActivity", "onSupportNavigateUp: ${previousBackStackEntry?.destination?.id}")
        return if (previousBackStackEntry?.destination?.id == null) {
            finish()
            true
        } else {
            artikelNavController.navigateUp() || super.onSupportNavigateUp()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val previousBackStackEntry = artikelNavController.previousBackStackEntry
        Log.d("ArtikelActivity", "onSupportNavigateUp [old]: ${previousBackStackEntry?.destination?.id}")
        if (previousBackStackEntry?.destination?.id == null) {
            finish()
        } else {
            if (!artikelNavController.navigateUp()) {
                super.onBackPressed()
            }
        }
    }
}