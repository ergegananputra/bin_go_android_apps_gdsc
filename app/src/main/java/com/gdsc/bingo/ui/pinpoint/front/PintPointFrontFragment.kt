package com.gdsc.bingo.ui.pinpoint.front

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdsc.bingo.databinding.FragmentPintPointFrontBinding
import androidx.navigation.fragment.findNavController

class PintPointFrontFragment : Fragment() {
    private val binding by lazy {
        FragmentPintPointFrontBinding.inflate(layoutInflater)
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
        setupCardSearch()
    }

    private fun setupCardSearch() {
        binding.pinPointFrontCardSearch.setOnClickListener {
            val destination = PintPointFrontFragmentDirections.actionPintPointFrontFragmentToSearchMapsFragment()
            findNavController().navigate(destination)
        }
    }


}