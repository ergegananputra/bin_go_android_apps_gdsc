package com.gdsc.bingo.ui.form_post

import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.ActivityFormPostBinding
import com.gdsc.bingo.ui.form_post.viewmodel.FormPostViewModel

class FormPostActivity : AppCompatActivity() {
    val args : FormPostActivityArgs by navArgs()
    private lateinit var formNavController : NavController

    private val binding by lazy {
        ActivityFormPostBinding.inflate(layoutInflater)
    }

    private val formViewModel by lazy {
        ViewModelProvider(this)[FormPostViewModel::class.java]
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

        formViewModel.clear()
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragmentFormPost.id) as NavHostFragment
        formNavController = navHostFragment.navController

        setSupportActionBar(binding.formPostToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun setupActionMenu(
        action: () -> Boolean
    ) {
        binding.formPostToolbar.setOnMenuItemClickListener {menuItem ->
            if (menuItem.itemId != R.id.form_post_header_button_save) return@setOnMenuItemClickListener false

            action()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        formViewModel.clear()
    }

    override fun onSupportNavigateUp(): Boolean {
        val previousBackStackEntry = formNavController.previousBackStackEntry
        return if (previousBackStackEntry?.destination?.id == null) {
            finish()
            true
        } else {
            formNavController.navigateUp() || super.onSupportNavigateUp()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val previousBackStackEntry = formNavController.previousBackStackEntry
        if (previousBackStackEntry?.destination?.id == null) {
            finish()
        } else {
            if (!formNavController.navigateUp()) {
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.form_post_menu, menu)
        return true
    }


    fun setToolbarTitle(fragment:Fragment, customTitle: String? = null) {
        if (customTitle != null) {
            binding.formPostToolbar.title = customTitle
        } else {
            when (fragment) {
                is FormPostFragment -> {
                    binding.formPostToolbar.title = "Buat Konten"
                }
                is FormFullEditorFragment -> {
                    binding.formPostToolbar.title = "Edit Deskripsi"
                }
                is ReportMapsFragment -> {
                    binding.formPostToolbar.title = "Pilih Lokasi"
                }
            }
        }
    }

    fun hideToolbar() {
        binding.formPostToolbar.isVisible = false
    }



}