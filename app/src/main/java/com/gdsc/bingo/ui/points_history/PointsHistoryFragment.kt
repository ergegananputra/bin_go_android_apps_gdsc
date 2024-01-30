package com.gdsc.bingo.ui.points_history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentPointsHistoryBinding


class PointsHistoryFragment : Fragment() {
    private val binding by lazy {
        FragmentPointsHistoryBinding.inflate(layoutInflater)

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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // TODO : setup recycler view

        updateRecyclerData()
    }

    private fun updateRecyclerData() {
        // TODO : update data recycler view
    }

    private fun setupBackButton() {
        binding.pointsHistoryButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


}