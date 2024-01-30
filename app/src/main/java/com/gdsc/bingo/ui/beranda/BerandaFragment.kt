package com.gdsc.bingo.ui.beranda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentBerandaBinding
import com.gdsc.bingo.ui.profil.ProfilFragmentDirections

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomNavigationVisibility(this)

        setupBinPoints()
        setupKomunitasBinGo()
    }

    private fun setupKomunitasBinGo() {
        binding.berandaButtonGoToKomunitas.setOnClickListener {
            (activity as MainActivity).binding.mainBottomNavigation.selectedItemId = R.id.navigation_komunitas
        }
    }


    private fun setupBinPoints() {
        val destination = BerandaFragmentDirections.actionNavigationBerandaToPointsHistoryFragment()

        binding.include.componentCardBinPoints.setOnClickListener {
            findNavController().navigate(destination)
        }
    }

}