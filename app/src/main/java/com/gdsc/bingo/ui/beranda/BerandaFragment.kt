package com.gdsc.bingo.ui.beranda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentBerandaBinding

class BerandaFragment : Fragment() {

    private val binding by lazy {
        FragmentBerandaBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

}