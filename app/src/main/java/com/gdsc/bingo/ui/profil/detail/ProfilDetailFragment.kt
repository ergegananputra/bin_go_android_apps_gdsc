package com.gdsc.bingo.ui.profil.detail

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.PatternsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentProfilDetailBinding
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.preferences.AppPreferences
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class ProfilDetailFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var isLogin = true

    private val binding by lazy {
        FragmentProfilDetailBinding.inflate(layoutInflater)
    }

    private var imageUri : Uri? = null
    private val openImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            loadProfilImage(imageUri.toString())

            Toast.makeText(requireContext(), "Foto Profil Berhasil Diubah", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadProfilImage(profilePictureUrl: String) {
        val urlWithTimestamp : String = if (profilePictureUrl.startsWith("http")) {
            "$profilePictureUrl?timestamp=${System.currentTimeMillis()}"
        } else {
            profilePictureUrl
        }

        binding.profilDetailImageView.load(urlWithTimestamp) {
            crossfade(true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.nav_ic_profil)
            error(R.drawable.nav_ic_profil)
        }

        Log.i("ProfilDetailFragment", "loadProfilImage: $urlWithTimestamp")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        (activity as MainActivity).setBottomNavigationVisibility(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = Firebase.storage("gs://bingo-fdbdb.appspot.com")

        switchToLogin()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwitchButton()
        setupSimpanButton()
        setupChangeImageProfilButton()

        setupErrorTextObserverClearance()
    }

    private fun setupErrorTextObserverClearance() {
        textErrorClear(binding.profilDetailTextInputLayoutUsername)
        textErrorClear(binding.profilDetailTextInputLayoutEmail)
        textErrorClear(binding.profilDetailTextInputLayoutPassword)
        textErrorClear(binding.profilDetailTextInputLayoutReassurePassword)
    }


    private fun textErrorClear(view: View) {
        val textInputLayout = view as TextInputLayout

        textInputLayout.editText!!.doAfterTextChanged {
            val isError = textInputLayout.error != null
            val text = it.toString()
            if (text.isNotEmpty() && isError) {
                textInputLayout.error = null
            }
        }
    }

    private fun setupChangeImageProfilButton() {
        binding.profilDetailButtonUbahFotoProfil.setOnClickListener {
            pickPhoto()
        }
    }

    private fun setupSimpanButton() {
        binding.profilDetailButtonSimpan.setOnClickListener {
            if (isLogin) {
                login()
            } else {
                register()
            }
        }
    }



    private fun register() {
        val username = binding.profilDetailTextInputLayoutUsername.editText!!.text.toString().trim()
        val email = binding.profilDetailTextInputLayoutEmail.editText!!.text.toString().trim()
        val password = binding.profilDetailTextInputLayoutPassword.editText!!.text.toString().trim()
        val reassurePassword = binding.profilDetailTextInputLayoutReassurePassword.editText!!.text.toString().trim()

        if (username.isEmpty()) {
            val errMsg = getString(R.string.username_tidak_valid)
            binding.profilDetailTextInputLayoutUsername.error = errMsg
            return
        }
        if (email.isEmpty() || PatternsCompat.EMAIL_ADDRESS.matcher(email).matches().not() ) {
            val errMsg = getString(R.string.email_tidak_valid)
            binding.profilDetailTextInputLayoutEmail.error = errMsg
            return
        }
        if (password.isEmpty() || password.length < 8) {
            val errMsg = getString(R.string.password_tidak_valid)
            binding.profilDetailTextInputLayoutPassword.error = errMsg
            return
        }
        if (password != reassurePassword) {
            val errMsg = getString(R.string.password_tidak_sama)
            binding.profilDetailTextInputLayoutReassurePassword.error = errMsg
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    val path = imageUri?.let {
                        val profilePictureStorageRef = storage.reference.child("profile_pictures/${auth.currentUser!!.uid}")
                        val file = imageUri
                        val userProfilRef = profilePictureStorageRef.child(file!!.lastPathSegment!!)

                        val uploadTask = userProfilRef.putFile(file)

                        uploadTask.addOnFailureListener {
                            Toast.makeText(requireContext(), "Upload Foto Profil Gagal", Toast.LENGTH_SHORT).show()
                        }.addOnSuccessListener {
                            Toast.makeText(requireContext(), "Upload Foto Profil Berhasil", Toast.LENGTH_SHORT).show()
                        }

                        userProfilRef.path
                    }


                    val uid = auth.currentUser!!.uid
                    val newUser = User(
                        referencePath = firestore.collection(User().table).document(uid),
                        username = username,
                        profilePicturePath = path,
                    )
                    firestore.collection(newUser.table).document(uid).set(newUser)

                    Toast.makeText(requireContext(), "Register Berhasil, mohon Login untuk melanjutkan", Toast.LENGTH_LONG).show()
                    switchToLogin()

                } else {
                    val errMsg = it.exception?.message
                    binding.profilDetailTextInputLayoutEmail.error = errMsg
                }
            }



    }

    private fun login() {
        val username = binding.profilDetailTextInputLayoutUsername.editText!!.text.toString().trim()
        val email = binding.profilDetailTextInputLayoutEmail.editText!!.text.toString().trim()
        val password = binding.profilDetailTextInputLayoutPassword.editText!!.text.toString().trim()

        if (username.isEmpty()) {
            val errMsg = getString(R.string.username_tidak_valid)
            binding.profilDetailTextInputLayoutUsername.error = errMsg
            return
        }
        if (email.isEmpty() || PatternsCompat.EMAIL_ADDRESS.matcher(email).matches().not() ) {
            val errMsg = getString(R.string.email_tidak_valid)
            binding.profilDetailTextInputLayoutEmail.error = errMsg
            return
        }
        if (password.isEmpty() || password.length < 8) {
            val errMsg = getString(R.string.password_tidak_valid)
            binding.profilDetailTextInputLayoutPassword.error = errMsg
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Login Berhasil", Toast.LENGTH_SHORT).show()

                    AppPreferences(requireContext()).apply {
                        userId = auth.currentUser!!.uid
                        userName = username
                        userEmail = email
                    }

                    findNavController().navigateUp()
                } else {
                    val errMsg = it.exception?.message
                    binding.profilDetailTextInputLayoutEmail.error = errMsg
                }
            }
    }

    private fun setupSwitchButton() {
        binding.profilDetailButtonSwapToRegister.setOnClickListener {
            if (isLogin) {
                switchToRegister()
            } else {
                switchToLogin()
            }
        }

    }

    private fun switchToRegister() {
        isLogin = false
        binding.profilDetailTextInputLayoutReassurePassword.visibility = View.VISIBLE
        binding.profilDetailButtonSwapToRegister.text = getString(R.string.sudah_punya_akun_login)
        binding.profilDetailButtonSimpan.text = getString(R.string.daftar)

        binding.profilDetailContainerImageView.visibility = View.VISIBLE
        binding.profilDetailButtonUbahFotoProfil.visibility = View.VISIBLE
    }

    private fun switchToLogin() {
        isLogin = true
        binding.profilDetailTextInputLayoutReassurePassword.visibility = View.GONE
        binding.profilDetailButtonSwapToRegister.text = getString(R.string.belum_punya_akun_daftar)
        binding.profilDetailButtonSimpan.text = getString(R.string.login)

        binding.profilDetailContainerImageView.visibility = View.GONE
        binding.profilDetailButtonUbahFotoProfil.visibility = View.GONE
    }


    private fun pickPhoto() {
        val mediaStoreIntent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        openImageLauncher.launch(mediaStoreIntent)
    }
}