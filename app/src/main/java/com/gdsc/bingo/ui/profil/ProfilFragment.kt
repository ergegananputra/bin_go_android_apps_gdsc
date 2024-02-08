package com.gdsc.bingo.ui.profil

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentProfilBinding
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.preferences.AppPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class ProfilFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    private val appPreferences by lazy {
        AppPreferences(requireContext())
    }

    private val binding by lazy {
        FragmentProfilBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        (activity as MainActivity).setBottomNavigationVisibility(this)
        preLoad()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
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
        binding.profileCardProfileTextViewEmail.text = email
    }

    private fun setupTextName(nama: String) {
        binding.profilCardProfilTextViewName.text = nama
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCardProfil()
        setupCardProfilPicture()
        setupBinPoints()
    }

    private fun setupBinPoints() {
        val destination = ProfilFragmentDirections.actionNavigationProfilToPointsHistoryFragment()

        binding.profilIncludeBinPoints.componentCardBinPoints.setOnClickListener {
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

    private fun loadOrRefreshPicture(forceUpdate: Boolean = false) {
        if (appPreferences.isUserLoggedIn().not()) {
            return
        }

        val source = if (forceUpdate) {
            Source.SERVER
        } else {
            Source.DEFAULT
        }

        firestore.collection(User().table).document(appPreferences.userId).get(source)
            .addOnSuccessListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val user = it.toObject(User::class.java)
                    if (user != null) {
                        val profilePicturePath = user.profilePicturePath
                        if (profilePicturePath.isNullOrEmpty().not()) {
                            val pictureRef = storage.reference.child(profilePicturePath!!)
                            val localFile = File(requireContext().cacheDir, "user_personal_profile_picture.jpg")

                            if (localFile.exists().not() || forceUpdate) {
                                pictureRef.getFile(localFile).addOnSuccessListener {
                                    Log.d("ProfilFragment", "Picture loaded : $localFile")
                                    binding.profilCardProfilImage.load(localFile) {
                                        crossfade(true)
                                        transformations(CircleCropTransformation())
                                        placeholder(R.drawable.nav_ic_profil)
                                        error(R.drawable.nav_ic_profil)
                                    }

                                }.addOnFailureListener {
                                    Log.e("ProfilFragment", "Error when loading picture", it)
                                }
                            } else {
                                binding.profilCardProfilImage.load(localFile) {
                                    crossfade(true)
                                    transformations(CircleCropTransformation())
                                    placeholder(R.drawable.nav_ic_profil)
                                    error(R.drawable.nav_ic_profil)
                                }
                            }




                        }
                    } else {
                        Log.e("ProfilFragment", "User not found")
                    }

                }
            }
            .addOnFailureListener {
               Log.e("ProfilFragment", "Error when loading picture", it)
            }
    }

    private fun setupCardProfil() {
        if (appPreferences.isUserLoggedIn()) {
            val name = appPreferences.userName
            val email = appPreferences.userEmail
            setupTextName(name)
            setupTextEmail(email)
            loadOrRefreshPicture()

            val dialog = MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Apakah anda yakin ingin log out?")
                .setPositiveButton("Ya, log out") { _, _ ->
                    auth.signOut()
                    appPreferences.clear()
                    setupCardProfil()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            binding.profilCardProfilContainer.setOnClickListener {
                dialog.show()
            }

        } else {
            setupTextName(getString(R.string.anda_belum_login))
            setupTextEmail(getString(R.string.klik_disini_untuk_login))

            val destination = ProfilFragmentDirections.actionNavigationProfilToNavigationProfilDetail()
            binding.profilCardProfilContainer.setOnClickListener {
                findNavController().navigate(destination)
            }
        }

    }

}