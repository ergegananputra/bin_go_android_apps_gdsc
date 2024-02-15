package com.gdsc.bingo.ui.form_post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentFormPostBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.KomentarHub
import com.gdsc.bingo.model.Likes
import com.gdsc.bingo.model.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FormPostFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val binding by lazy {
        FragmentFormPostBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        (activity as MainActivity).setBottomNavigationVisibility(this)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
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
            }
        }
    }

    private suspend fun submitForm() {
        withContext(Dispatchers.IO) {
            val title = binding.formPostTextInputLayoutTitle.getTrimEditText() ?: run {
                val errMsg = getString(R.string.error_title_required)
                binding.formPostTextInputLayoutTitle.error = errMsg
                return@withContext
                }
            val caption = binding.formPostTextInputLayoutCaption.getTrimEditText() ?: run {
                val errMsg = getString(R.string.error_caption_required)
                binding.formPostTextInputLayoutCaption.error = errMsg
                return@withContext
                }

            val videoLink = binding.formPostTextInputLayoutVideoLink.getTrimEditText()

            val forums = Forums(
                title = title,
                author = firestore.collection(User().table).document(auth.uid!!),
                videoLink = videoLink,
                createdAt = Timestamp.now()
            )

            if (caption.length > forums.textLimit) {
                try {
                    forums.isUsingTextFile = true
                    forums.textFilePath = uploadFile(caption)
                } catch (e: Exception) {
                    Log.e("FormPostFragment", "submitForm: ${e.message}")

                    return@withContext executeFailure()
                }
            } else {
                forums.text = caption
            }

            // TODO: Upload thumbnail dan photos

            uploadForums(forums)
        }
    }


    private fun generateRandomString(length: Int): String {
        val chars = (' '..'~')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }


    private fun uploadForums(forums: Forums) {
        CoroutineScope(Dispatchers.IO).launch {

            firestore.collection(forums.table).add(forums.toFirebaseModel())
                .addOnSuccessListener { forumPostRef ->
                    val documentId = forumPostRef.id
                    forums.apply {
                        referencePath = forumPostRef
                        likesReference = firestore.collection(Likes().table).document(documentId)
                        komentarHub = firestore.collection(KomentarHub().table).document(documentId)
                    }
                    forumPostRef.update(forums.toFirebaseModel())

                    // Create KomentarHub and fill up empty fields
                    val komentarHub = KomentarHub(
                        referencePath = forums.komentarHub,
                        createdAt = Timestamp.now()
                    )
                    forums.komentarHub?.set(komentarHub.toFirebaseModel())
                        ?.addOnFailureListener {
                            Log.e("FormPostFragment", "uploadForums: ${it.message}")
                            executeFailure()
                        }
                        ?.addOnSuccessListener {
                            Log.i("FormPostFragment", "uploadForums: KomentarHub created")
                            findNavController().navigateUp()
                        }
                }
                .addOnFailureListener {
                    Log.e("FormPostFragment", "uploadForums: ${it.message}")
                    executeFailure()
                }

        }
    }

    private fun executeFailure() {
        val errMesg = getString(R.string.error_upload_file)
        Toast.makeText(requireContext(), errMesg, Toast.LENGTH_LONG).show()

        // TODO : handle error
    }

    private suspend fun uploadFile(caption: String): String = suspendCoroutine { continuation ->
        val storageRef = storage.reference
        val fileForumsReference = storageRef.child("public/${Forums().table}/${auth.uid}_${Timestamp.now().nanoseconds}.txt")


        val data = caption.toByteArray(Charsets.UTF_8)

        val uploadTask = fileForumsReference.putBytes(data)
        uploadTask.addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(fileForumsReference.path)
            } else {
                Log.e("FormPostFragment", "uploadFile: ${it.exception}")
                continuation.resumeWithException(it.exception!!)
            }
        }

    }

    private fun setupBackButton() {
        binding.formPostHeaderButtonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun View.getTrimEditText() : String? {
        val view = this as? TextInputLayout
        return view?.editText?.text.toString().trim().let {
            it.ifEmpty { null }
        }
    }



}