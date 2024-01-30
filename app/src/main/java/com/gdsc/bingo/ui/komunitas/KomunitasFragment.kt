package com.gdsc.bingo.ui.komunitas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentKomunitasBinding

class KomunitasFragment : Fragment() {
    private val binding by lazy {
        FragmentKomunitasBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomNavigationVisibility(this)


        setupCreateKomunitasExtendedFloatingActionButton()
    }

    private fun setupCreateKomunitasExtendedFloatingActionButton() {
        val destination = KomunitasFragmentDirections.actionNavigationKomunitasToFormPostFragment()
        binding.komunitasExtendedFloatingActionButton.setOnClickListener {
            findNavController().navigate(destination)
        }

        // TODO : Expand and collapse extended floating action button based on scroll in recycler view
    }



}