package com.gdsc.bingo.ui.form_post

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.ui.form_post.viewmodel.FormPostViewModel
import com.gdsc.bingo.ui.pop_up.SuccesReportPopUp

class FormPostActivity : AppCompatActivity() {
    val args : FormPostActivityArgs by navArgs()
    lateinit var formNavController : NavController

    private val binding by lazy {
        ActivityFormPostBinding.inflate(layoutInflater)
    }

    private val formViewModel by lazy {
        ViewModelProvider(this)[FormPostViewModel::class.java]
    }

    private val animationLottieLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("FormPostActivity", "onActivityResult: ${result.resultCode}")
        finish()
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
        Log.d("FormPostActivity", "onSupportNavigateUp: ${previousBackStackEntry?.destination?.id}")
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
        Log.d("FormPostActivity", "onBackPressed: ${previousBackStackEntry?.destination?.id}")
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

    fun showToolbar() {
        binding.formPostToolbar.isVisible = true
    }

    fun startDoneAnimation(type: String) {
        when (type) {
            Forums.ForumType.REPORT.fieldName -> {
                val intent = Intent(this, SuccesReportPopUp::class.java)
                animationLottieLauncher.launch(intent)
            }
            else -> {
                finish()
            }
        }

    }



}