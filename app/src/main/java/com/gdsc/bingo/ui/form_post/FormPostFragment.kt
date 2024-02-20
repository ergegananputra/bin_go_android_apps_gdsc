package com.gdsc.bingo.ui.form_post

import android.app.Activity
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.adapter.ImagePostAdapter
import com.gdsc.bingo.databinding.FragmentFormPostBinding
import com.gdsc.bingo.model.FireModel
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.KomentarHub
import com.gdsc.bingo.model.Likes
import com.gdsc.bingo.model.PostImage
import com.gdsc.bingo.model.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

    private val imagePostAdapter = ImagePostAdapter(
        storage = FirebaseStorage.getInstance(),
        useDeleteButton = true,
        onDeleteItem = { position ->
            onDeleteImage(position)
            checkListEmpty()
        }
    )

    private fun checkListEmpty() {
        if (imagePostAdapter.currentList.isEmpty()) {
            binding.formPostImageContainer.visibility = View.GONE
            binding.formPostTextViewNoImageUploaded.visibility = View.VISIBLE
        } else {
            binding.formPostImageContainer.visibility = View.VISIBLE
            binding.formPostTextViewNoImageUploaded.visibility = View.GONE
        }
    }

    private fun onDeleteImage(position: Int) {
        val lists = imagePostAdapter.currentList.toMutableList()

        if (lists.size == 1) {
            lists.clear()
            imagePostAdapter.submitList(lists)
            return
        }

        lists.removeAt(position)
        imagePostAdapter.submitList(lists)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        (activity as MainActivity).setStatusAndBottomNavigation(this)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupSaveButton()
        setupImageRecycler()
        setupAddImageButton()

    }

    private val openImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uri = data?.data
            val filename = data?.data?.lastPathSegment
            val image = PostImage(
                path = uri.toString(),
                filename = filename,
                createAt = Timestamp.now()
            )

            val list = imagePostAdapter.currentList.toMutableList()
            list.add(image)
            imagePostAdapter.submitList(list)

            checkListEmpty()
        }

    }

    private fun setupAddImageButton() {
        binding.formPostButtonUnggahGambar.setOnClickListener {
            pickPhoto()
        }
    }

    private fun pickPhoto() {
        val mediaStoreIntent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        openImageLauncher.launch(mediaStoreIntent)
    }

    private fun setupImageRecycler() {
        binding.formPostImageContainer.apply {
            adapter = imagePostAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
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

            val videoLink = binding.formPostTextInputLayoutVideoLink.getTrimEditText().getYoutubeVideoId()

            val forums = Forums(
                title = title,
                author = firestore.collection(User().table).document(auth.uid!!),
                videoLink = videoLink,
                createdAt = Timestamp.now()
            )

            uploadForums(forums, caption)
        }
    }


    private fun generateRandomString(length: Int): String {
        val chars = (' '..'~')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }


    private fun uploadForums(forums: Forums, caption: String) {
        val uploadToken : MutableLiveData<Int> = MutableLiveData(1)

        fun addUploadToken() {
            CoroutineScope(Dispatchers.Main).launch(Dispatchers.IO) {
                uploadToken.postValue(uploadToken.asFlow().first().plus(1))
            }
        }

        fun removeUploadToken() {
            CoroutineScope(Dispatchers.Main).launch(Dispatchers.IO) {
                uploadToken.postValue(uploadToken.asFlow().first().minus(1))
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            uploadToken.observe(viewLifecycleOwner) {
                if (it == 0) {
                    findNavController().navigateUp()
                }
            }
        }


        CoroutineScope(Dispatchers.IO).launch {

            firestore.collection(forums.table).add(forums.toFirebaseModel())
                .addOnSuccessListener { forumPostRef ->
                    val documentId = forumPostRef.id

                    CoroutineScope(Dispatchers.IO).launch update@{
                        // Upload text file
                        if (caption.length > forums.textLimit) {

                            try {
                                forums.isUsingTextFile = true
                                forums.textFilePath = uploadFile(caption, documentId)
                            } catch (e: Exception) {
                                Log.e("FormPostFragment", "submitForm: ${e.message}")

                                Log.w("FormPostFragment", "submitForm: Deleting forums")
                                forumPostRef.delete()

                                return@update executeFailure()
                            }

                        } else {
                            forums.text = caption
                        }

                        forums.apply {
                            referencePath = forumPostRef
                            likesReference = firestore.collection(Likes().table).document(documentId)
                            komentarHub = firestore.collection(KomentarHub().table).document(documentId)
                        }
                        forumPostRef.update(forums.toFirebaseModel())
                    }




                    // Upload images
                    if (imagePostAdapter.currentList.isNotEmpty()) {

                        val imageRef = forumPostRef.collection(PostImage().table)
                        imagePostAdapter.currentList.forEachIndexed { index, image ->
                            val photosRef = storage.reference
                                .child("public/${forums.table}/${forumPostRef.id}/${image.table}/${image.filename}_${image.createAt?.seconds}")

                            val file = Uri.parse(image.path)

                            val uploadTask = photosRef.putFile(file)

                            addUploadToken()

                            uploadTask.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    image.path = photosRef.path

                                    addUploadToken()
                                    imageRef.add(image.toFirebaseModel())
                                        .addOnSuccessListener { onSuccessReference ->
                                            image.referencePath = onSuccessReference

                                            addUploadToken()
                                            onSuccessReference.update(FireModel.Keys.referencePath, onSuccessReference)
                                                .addOnSuccessListener {
                                                    Log.i("FormPostFragment", "uploadForums: Image uploaded")

                                                    if (index == 0) {
                                                        addUploadToken()

                                                        forumPostRef.update(Forums.Keys.thumbnailPhotosUrl, image.path)
                                                            .addOnSuccessListener {
                                                                Log.i("FormPostFragment", "uploadForums: Thumbnail updated")
                                                                removeUploadToken()
                                                            }
                                                            .addOnFailureListener {
                                                                Log.e("FormPostFragment", "uploadForums: failed when update thumbnail \n ${it.message}")
                                                                executeFailure()
                                                            }
                                                    }

                                                    removeUploadToken()
                                                }
                                                .addOnFailureListener {
                                                    Log.e("FormPostFragment", "uploadForums: failed when update image reference path \n ${it.message}")
                                                    executeFailure()
                                                }

                                            removeUploadToken()
                                        }
                                        .addOnFailureListener {
                                            Log.e("FormPostFragment", "uploadForums: ${it.message}")
                                            executeFailure()
                                        }
                                } else {
                                    Log.e("FormPostFragment", "uploadForums: ${task.exception}")
                                    executeFailure()
                                }

                                removeUploadToken()
                            }


                        }
                    }


                    // Create KomentarHub and fill up empty fields
                    addUploadToken()

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
                            removeUploadToken()
                        }

                    removeUploadToken()
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

    private suspend fun uploadFile(
        caption: String,
        documentId: String
    ): String = suspendCoroutine { continuation ->
        val storageRef = storage.reference
        val fileForumsReference = storageRef.child("public/${Forums().table}/${documentId}/${auth.uid}_${Timestamp.now().nanoseconds}.txt")


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

private fun String?.getYoutubeVideoId(): String? {
    if (this.isNullOrEmpty()) return null

    // Youtube Filter
    val youtubeVideoID = if (this.contains("v=") && this.contains("youtube.com")) {
        val c1 = this.split("v=")[1]

        if (c1.contains("&")) {
            c1.split("&")[0]
        } else {
            c1
        }

    } else if (this.contains("youtu.be")){
        val c1 = this.split("youtu.be/")[1]

        if (c1.contains("?")) {
            c1.split("?")[0]
        } else {
            c1
        }
    } else {
        null
    }


    return youtubeVideoID
}
