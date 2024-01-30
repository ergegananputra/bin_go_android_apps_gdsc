package com.gdsc.bingo.ui.points_history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return binding.root
    }


}