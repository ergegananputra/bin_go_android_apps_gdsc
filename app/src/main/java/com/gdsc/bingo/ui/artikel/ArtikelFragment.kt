package com.gdsc.bingo.ui.artikel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.facebook.shimmer.ShimmerFrameLayout
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.adapter.KomentarAdapter
import com.gdsc.bingo.databinding.FragmentArtikelBinding
import com.gdsc.bingo.model.FireModel
import com.gdsc.bingo.model.Forums.Keys.commentCount
import com.gdsc.bingo.model.Komentar
import com.gdsc.bingo.model.Like
import com.gdsc.bingo.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId



class ArtikelFragment : Fragment() {
    private val navArgs by navArgs<ArtikelFragmentArgs>()

    private lateinit var fireStore : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage

    private val binding by lazy { FragmentArtikelBinding.inflate(layoutInflater) }

    private val komenAdapter = KomentarAdapter(
        storage = FirebaseStorage.getInstance()
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        setupShimmer()
        setupRecyclerViewComment()
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        return binding.root
    }

    private fun setupShimmer() {
        with(binding) {
            artikelShimmerTitle.start()
            artikelShimmerUsername.start()
            artikelShimmerTimestamp.start()
            artikelShimmerImageContent.start()
            artikelShimmerContent.start()
            artikelShimmerLikeAndCommentCount.start()
        }
    }

    private fun ShimmerFrameLayout.start() {
        this.startShimmer()
        this.visibility = View.VISIBLE
    }

    private fun ShimmerFrameLayout.stop() {
        this.stopShimmer()
        this.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomNavigationVisibility(this)

        setupToolbarButton()
        lifecycleScope.launch(Dispatchers.Main) {
            this.launch { setupTitle() }
            this.launch { setupProfile() }
            this.launch{ setupTimestamp(navArgs.createAtSeconds) }
            this.launch(Dispatchers.IO) { setupImagesContent(navArgs.thumbnailPhotosUrl) }
            this.launch { setupTextContent(navArgs.text, navArgs.isUsingTextFile, navArgs.textFilePathDocumentString) }
            this.launch { setupLikeAndCommentCount(navArgs.likeCount, navArgs.dislikeCount, navArgs.commentCount) }
            this.launch(Dispatchers.IO) { setupVideoContent(navArgs.videoLink) }
            this.launch(Dispatchers.IO) { loadRecyclerCommentData() }
            this.launch(Dispatchers.Main) { setupKomentar() }
        }
        setupLikeButton()


    }

    private suspend fun setupKomentar() {
        if (auth.uid == null) {
            Log.e("ArtikelFragment", "User is not logged in")
            binding.artikelTextInputLayoutKomentar.visibility = View.GONE
            return
        } else {
            binding.artikelTextInputLayoutKomentar.visibility = View.VISIBLE
        }

        val currentUser = fireStore.collection(User().table).document(auth.uid!!).get().await()

        binding.artikelTextInputLayoutKomentar.setEndIconOnClickListener {
            val text = binding.artikelTextInputLayoutKomentar.editText?.text.toString()
            val komentarHub = fireStore.document(navArgs.komentarHubDocumentString!!)
            val user = User().toModel(currentUser)

            val komentar = Komentar(
                referencePath = komentarHub,
                komentar = text,
                profilePicturePath = user.profilePicturePath,
                username = user.username,
                createdAt = Timestamp.now()
            )

            fireStore.document(navArgs.komentarHubDocumentString!!)
                .collection(komentar.table)
                .add(komentar.toFirebaseModel())
                .addOnSuccessListener {
                    val newCommentCount = navArgs.commentCount + 1
                    fireStore.document(navArgs.referenecePathDocumentString)
                        .update(commentCount, newCommentCount)

                    setupLikeAndCommentCount(navArgs.likeCount, navArgs.dislikeCount, newCommentCount)

                    binding.artikelTextInputLayoutKomentar.apply {
                        editText?.text?.clear()
                        clearFocus()
                    }

                    loadRecyclerCommentData()
                }
        }
    }

    private fun loadRecyclerCommentData() {
        lifecycleScope.launch(Dispatchers.Main) {
            val komentarHubDocumentString = navArgs.komentarHubDocumentString ?: run {
                Log.e("ArtikelFragment", "KomentarHubDocumentString is null")
                return@launch
            }

            val komentarHub = withContext(Dispatchers.IO) {
                fireStore.document(komentarHubDocumentString)
                    .collection(Komentar().table)
                    .orderBy(FireModel.Keys.createdAt, Query.Direction.DESCENDING)
                    .get()
                    .await()
            }

            val comments = Komentar().toModels(komentarHub)
            komenAdapter.submitList(comments)
        }
    }

    private fun setupLikeButton() {
        var initialState = true

        if (auth.uid == null) {
            Log.e("ArtikelFragment", "User is not logged in")
            binding.artikelButtonLike.visibility = View.INVISIBLE
            return
        } else {
            binding.artikelButtonLike.visibility = View.VISIBLE
        }

        fireStore.document(navArgs.likesReference)
            .collection(Like().table)
            .document(auth.uid!!)
            .get(Source.SERVER)
            .addOnSuccessListener { documentReference ->
                if (documentReference.exists()) {
                    likeUI()
                    initialState = true
                    return@addOnSuccessListener
                } else {
                    unlikeUI()
                    initialState = false
                }
            }

        binding.artikelButtonLike.setOnCheckedChangeListener { _, isChecked ->
            val forumRef = fireStore.document(navArgs.referenecePathDocumentString)
            val likeCount = navArgs.likeCount
            val dislikeCount = navArgs.dislikeCount
            val user = fireStore.collection(User().table).document(auth.uid!!)
            val author = fireStore.document(navArgs.authorDocumentString)

            if (user.path == author.path) {
                val msg = getString(R.string.you_cant_like_your_own_post)
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            val like = Like(
                referencePath = forumRef,
                owner = user,
                objectPerson = author,
                createAt = Timestamp.now()
            )


            fireStore.document(navArgs.likesReference)
                .collection(like.table)
                .document(user.id)
                .get(Source.SERVER)
                .addOnSuccessListener { documentReference ->
                    if (isChecked) {
                        documentReference.reference.set(like.toFirebaseModel())
                            .addOnSuccessListener {

                                val newLike = if (initialState) likeCount else likeCount + 1

                                forumRef.update("like_count", newLike)
                                setupLikeAndCommentCount(newLike, navArgs.dislikeCount, navArgs.commentCount)
                            }
                        return@addOnSuccessListener
                    } else {
                        documentReference.reference.delete()
                            .addOnSuccessListener {

                                val newLike = if (initialState) likeCount - 1 else likeCount

                                forumRef.update("like_count", newLike)
                                setupLikeAndCommentCount(newLike, navArgs.dislikeCount, navArgs.commentCount)
                            }
                    }


                }

        }

    }

    private fun unlikeUI() {
        binding.artikelButtonLike.isChecked = false
    }

    private fun likeUI() {
        binding.artikelButtonLike.isChecked = true
    }

    private fun setupProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tempUser = User()
            val author = fireStore.document(navArgs.authorDocumentString).get().await()
            val user = tempUser.toModel(author)
            Log.d("ArtikelFragment", "User: $user")

            this.launch(Dispatchers.Main) { setupUsername(user.username!!) }
            this.launch { setupProfilePicture(user.profilePicturePath) }
        }
    }

    private fun setupRecyclerViewComment() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.artikelRecyclerViewCommentSection.apply {
                adapter = komenAdapter
                layoutManager = LinearLayoutManager(requireActivity())
            }

        }
    }

    private fun setupVideoContent(videoLink: String?) {
        //TODO: setup video content

        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(
                requireContext(),
                "Unimplemented setupVideoContent",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupLikeAndCommentCount(likeCount: Long, dislikeCount: Long, commentCount: Long) {
        val komen = getString(R.string.komentar)
        val suka = getString(R.string.suka)
        val text = "$commentCount $komen, $likeCount $suka"
        binding.artikelTextViewLikeAndCommentCount.text = text
        binding.artikelShimmerLikeAndCommentCount.stop()
    }

    private fun setupTextContent(
        text: String?,
        usingTextFile: Boolean,
        textFilePathDocumentString: String?,
    ) {
        if (usingTextFile.not()) {
            binding.artikelTextViewContent.text = text
            binding.artikelShimmerContent.stop()
            return
        }

        storage.getReference(textFilePathDocumentString!!).getBytes(10_000_000)
            .addOnSuccessListener { bytes ->
                val textData = String(bytes)
                binding.artikelTextViewContent.text = textData
                binding.artikelShimmerContent.stop()

            }
            .addOnFailureListener { exception ->
                Log.e("ArtikelFragment", "Error getting text file", exception)
                binding.artikelTextViewContent.text = "Error getting text file"
                binding.artikelShimmerContent.stop()
            }
    }

    private suspend fun setupImagesContent(thumbnailPhotosUrl: String?) {
        withContext(Dispatchers.Main) {
            if (thumbnailPhotosUrl.isNullOrEmpty()) {
                binding.artikelImageViewContent.visibility = View.GONE
                binding.artikelShimmerImageContent.stop()
                return@withContext
            }

            // TODO: Load image content
            Toast.makeText(requireContext(), "ThumbnailPhotosUrl: $thumbnailPhotosUrl", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTimestamp(seconds: Long) {
        val zonedDateTime = Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault())
        val month = "${zonedDateTime.month}".lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val text = "${zonedDateTime.dayOfMonth} $month ${zonedDateTime.year}, ${zonedDateTime.hour}:${zonedDateTime.minute}"
        binding.artikelTextViewTimestamp.text = text
        binding.artikelShimmerTimestamp.stop()
    }

    private fun setupProfilePicture(profilePicturePath: String?) {
        if (profilePicturePath.isNullOrEmpty()) {
            Log.i("ArtikelFragment", "Profile picture path is null")
            binding.artikelImageViewProfilePicture.load(R.drawable.ic_person_24) {
                transformations(CircleCropTransformation())
            }
            return
        }

        Log.i("ArtikelFragment", "Profile picture path: $profilePicturePath")

         storage.getReference(profilePicturePath).downloadUrl
             .addOnSuccessListener { uri ->
                binding.artikelImageViewProfilePicture.load(uri) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person_24)
                    error(R.drawable.ic_person_24)
                }
             }
             .addOnFailureListener { exception ->
                 Log.e("ArtikelFragment", "Error getting profile picture", exception)
                 binding.artikelImageViewProfilePicture.load(R.drawable.ic_person_24) {
                     transformations(CircleCropTransformation())
                 }
             }
    }

    private fun setupUsername(username: String) {
        binding.artikelTextViewUsername.text = username
        binding.artikelShimmerUsername.stop()
    }

    private fun setupToolbarButton() {
        binding.artikelToolbarButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun setupTitle() {
        val title = navArgs.title
        binding.artikelTextViewTitle.text = title
        binding.artikelShimmerTitle.stop()
    }


}