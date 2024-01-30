package com.gdsc.bingo.ui.profil

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.databinding.FragmentProfilBinding


class ProfilFragment : Fragment() {

    private val binding by lazy {
        FragmentProfilBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        preLoad()
        return binding.root
    }

    /**
     * [preLoad]
     *
     *
     * Digunakan untuk melakukan load data yang berukuran ringan sebelum fragment dibuat.
     * Harap dicatat bahwa jika melakukan load data yang berukuran besar, maka akan memperlambat
     * pembuatan fragment.
     */
    private fun preLoad() {
        // TODO : ambil shared preference dan ganti value point, nama dan email
        val sharedPreferences : Any? = null
        val nama = "Todo: Ambil Nama"
        val email = "Todo: Ambil Email"
        val point = 0 // TODO : ambil point dari shared preference

        setupTextName(nama)
        setupTextEmail(email)
        setTextPoint(point)
    }

    private fun setTextPoint(point: String) {
        // TODO : Format point
        binding.profilIncludeBinPoints.componentCardBinPointsTextViewPoints.text = point
    }

    private fun setTextPoint(point: Int) {
        setTextPoint(point.toString())
    }

    private fun setupTextEmail(email: String) {
        // TODO : ambil email dari shared preference
        binding.profileCardProfileTextViewEmail.text = email
    }

    private fun setupTextName(nama: String) {
        // TODO : ambil nama dari shared preference
        binding.profilCardProfilTextViewName.text = nama
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomNavigationVisibility(this)

        setupCardProfil()
        setupCardProfilPicture()
        setupBinPoints()
    }

    private fun setupBinPoints() {
        val destination = ProfilFragmentDirections.actionNavigationProfilToPointsHistoryFragment()

        binding.profilIncludeBinPoints.componentCardBinPoints.setOnClickListener {
            Log.v("ProfilFragment", "setupBinPoints: Clicked")
            findNavController().navigate(destination)
        }
    }

    private fun setupCardProfilPicture() {
        // TODO : load gambar dari storage dan aksi edit ketika klik

        loadOrRefreshPicture()
        binding.profilCardProfilPlaceholderImage.setOnClickListener {
            // TODO : aksi edit gambar
            Toast.makeText(requireContext(), "Todo: Aksi edit gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadOrRefreshPicture() {
        // TODO : load gambar dari storage ke
        // binding.profilCardProfilImage
        Toast.makeText(requireContext(), "Todo: Load gambar dari storage", Toast.LENGTH_SHORT).show()
    }

    private fun setupCardProfil() {
        binding.profilCardProfilContainer.setOnClickListener {
            // TODO: aksi login atau logout atau form edit profil
            Toast.makeText(requireContext(), "Todo: Aksi login atau logout", Toast.LENGTH_SHORT).show()
        }
    }

}