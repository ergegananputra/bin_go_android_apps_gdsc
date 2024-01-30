package com.gdsc.bingo.ui.form_post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentFormPostBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormPostFragment : Fragment() {
    private val binding by lazy {
        FragmentFormPostBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        (activity as MainActivity).setBottomNavigationVisibility(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupSaveButton()
        setupCarousel()
    }

    private fun setupCarousel() {
        // TODO : setup carousel
    }

    private fun setupSaveButton() {
        binding.formPostHeaderButtonSave.setOnClickListener {
            lifecycleScope.launch {
                submitForm()

                findNavController().navigateUp()
            }
        }
    }

    private suspend fun submitForm() {
        withContext(Dispatchers.IO) {
            // TODO : submit form

            this.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "TODO: Uploading", Toast.LENGTH_SHORT).show()
            }

            delay(1000L)

            this.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "TODO: Uploading Success", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackButton() {
        binding.formPostHeaderButtonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }



}