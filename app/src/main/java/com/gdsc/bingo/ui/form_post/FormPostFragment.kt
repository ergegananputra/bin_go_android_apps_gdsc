package com.gdsc.bingo.ui.form_post

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.bingo.R
import com.gdsc.bingo.adapter.ImagePostAdapter
import com.gdsc.bingo.databinding.FragmentFormPostBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.PostImage
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.textstyling.AddOnSpannableTextStyle
import com.gdsc.bingo.ui.form_post.viewmodel.FormPostViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class FormPostFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val binding by lazy {
        FragmentFormPostBinding.inflate(layoutInflater)
    }

    private val formViewModel by lazy {
        ViewModelProvider(requireActivity())[FormPostViewModel::class.java]
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
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        chooseFormType(binding, (activity as FormPostActivity).args.type)
        return binding.root
    }

    private fun chooseFormType(binding: FragmentFormPostBinding, type: String) {
        with(binding) {
            when (type) {
                Forums.ForumType.REPORT.fieldName -> {
                    formPostCardViewLocation.visibility = View.VISIBLE
                    formPostCardViewCategory.visibility = View.GONE
                    formPostTextInputLayoutVideoLink.visibility = View.GONE

                    (activity as FormPostActivity).setToolbarTitle(this@FormPostFragment, getString(R.string.report_title))
                }
                else -> {
                    formPostCardViewLocation.visibility = View.GONE
                    formPostCardViewCategory.visibility = View.VISIBLE
                    formPostTextInputLayoutVideoLink.visibility = View.VISIBLE

                    (activity as FormPostActivity).setToolbarTitle(this@FormPostFragment, getString(R.string.post))
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSaveButton()
        setupImageRecycler()
        setupAddImageButton()

        setupDescriptionText()
        setupButtonEditDescription()
        setupCategoryDropdown()

        setupLocationButton()
        setupLocationText()
    }

    private fun setupLocationText() {
        formViewModel.vicinity.observe(viewLifecycleOwner) { _ ->
            val address = formViewModel.address ?: getString(R.string.pilih_lokasi)
            binding.formPostTextViewLocation.text = address
        }
    }

    private fun setupLocationButton() {
        binding.formPostCardViewLocation.setOnClickListener {
            val action = FormPostFragmentDirections.actionFormPostFragment2ToReportMapsFragment2()
            findNavController().navigate(action)
        }
    }

    private fun setupCategoryDropdown() {
        val categoryPopUpWindow = ListPopupWindow(requireContext(), null, com.google.android.material.R.attr.listPopupWindowStyle)

        categoryPopUpWindow.anchorView = binding.formPostCardViewCategory

        // Set List Popup Content
        val categoryList = listOf("Plastik", "Kaleng", "Galon", "Kertas", "Minyak Jelantah")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_popup_window_item, categoryList)
        categoryPopUpWindow.setAdapter(adapter)


        // Set list popup item click listener
        categoryPopUpWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            // Respond to list popup window item click.
            val category = categoryList[position]
            appendChips(category)

            // Dismiss popup.
            categoryPopUpWindow.dismiss()
        }

        // Show list popup window on button click.
        binding.formPostButtonCategory.setOnClickListener {
            categoryPopUpWindow.show()
        }
    }

    private fun appendChips(category: String) {
        checkIfAlreadyExist(category).let { if (it) return }

        val chip = LayoutInflater.from(requireContext()).inflate(R.layout.item_chips_category, null) as Chip
        chip.text = category

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(4, 4, 4, 4)
        chip.layoutParams = params

        chip.setOnClickListener { view ->
            binding.formPostCategoryChipContainer.removeView(view)
        }

        binding.formPostCategoryChipContainer.addView(chip)
    }

    private fun checkIfAlreadyExist(category: String) : Boolean {
        var alreadyExist = false
        binding.formPostCategoryChipContainer.forEach {
            if ((it as Chip).text == category) {
                alreadyExist = true
                return@forEach
            }
        }
        return alreadyExist
    }

    private fun setupDescriptionText() {

        formViewModel.description.observe(viewLifecycleOwner) { rawHTML ->
            if (rawHTML.isNullOrEmpty()) {

                return@observe
            }

            val spannableConverter = AddOnSpannableTextStyle()

            val spanned = spannableConverter.convertHtmlWithOrderedList(rawHTML)

            binding.formPostTextViewDescription.text = spanned
            val typeface = Typeface.DEFAULT
            binding.formPostTextViewDescription.typeface = typeface
        }
    }

    private fun setupButtonEditDescription() {
        binding.formPostButtonEditDescription.setOnClickListener {
            val action = FormPostFragmentDirections.actionFormPostFragment2ToFormFullEditorFragment2()
            findNavController().navigate(action)
        }
    }

    private val openImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uri = data?.data
            val filename = data?.data?.lastPathSegment

            // Save the image to a local file
            val localFile = File(context?.filesDir, filename)
            val inputStream = context?.contentResolver?.openInputStream(uri!!)
            val outputStream = FileOutputStream(localFile)
            inputStream?.copyTo(outputStream)

            val image = PostImage(
                path = uri.toString(),
                filename = localFile.absolutePath,
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
        (activity as FormPostActivity).setupActionMenu(
            action = {
                lifecycleScope.launch {
                    val title = binding.formPostTextInputLayoutTitle.getTrimEditText() ?: run {
                        val errMsg = getString(R.string.error_title_required)
                        binding.formPostTextInputLayoutTitle.error = errMsg
                        return@launch
                    }
                    val caption = formViewModel.description.value ?: run {
                        val errMsg = getString(R.string.error_caption_required)
                        binding.formPostTextViewDescription.error = errMsg
                        return@launch
                    }

                    val videoLink = binding.formPostTextInputLayoutVideoLink.getTrimEditText().getYoutubeVideoId()


                    val uploadResult = submitForm(title, caption, videoLink)

                    if (uploadResult) {
                        (activity as FormPostActivity).let {
                            it.startDoneAnimation(it.args.type)
                        }
                    } else {
                        val errMsg = getString(R.string.error_upload_failed)
                        Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show()
                    }
                }
                true
            }
        )
    }

    private suspend fun submitForm(title : String, caption : String, videoLink : String?) : Boolean {
        val vicinity = withContext(Dispatchers.IO) {
            formViewModel.vicinity.asFlow().firstOrNull()
        }

        val forums = Forums(
            title = title,
            author = firestore.collection(User().table).document(auth.uid!!),
            videoLink = videoLink,
            createdAt = Timestamp.now(),
            type = (activity as FormPostActivity).args.type,
            vicinity = vicinity
        )

        if ((activity as FormPostActivity).args.type == Forums.ForumType.REPORT.fieldName
            && vicinity == null) {
            val errMsg = getString(R.string.error_location_required)
            Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show()
            return false
        }

        val imageList = mutableListOf<PostImage>()
        imageList.addAll(imagePostAdapter.currentList)

        return formViewModel.uploadForums(
            firestore,
            storage,
            auth,
            (activity as FormPostActivity).args.type,
            forums,
            caption,
            vicinity,
            binding.formPostTextViewLocation.text.toString(),
            imageList
        )

    }


    private fun generateRandomString(length: Int): String {
        val chars = (' '..'~')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
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