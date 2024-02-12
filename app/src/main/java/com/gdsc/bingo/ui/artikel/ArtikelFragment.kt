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
import coil.load
import coil.transform.CircleCropTransformation
import com.facebook.shimmer.ShimmerFrameLayout
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentArtikelBinding
import com.gdsc.bingo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        setupShimmer()
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
            this.launch(Dispatchers.IO) { setupRecyclerViewComment(navArgs.komentarHubDocumentString) }
        }
        setupLikeButton()

    }

    private fun setupLikeButton() {
        // TODO: setup like button
        /**
         * Saran solusi: gunakan collection baru dan tambahan documentReference ke forum agar
         * dapat mengetahui apakah user telah memberikan like atau tidak. Isi dari collection like
         * tersebut akan berisikan id user (pakai documentReference) dan orang yang pertama like adalah
         * pemilik forum. Maka untuk itu, gunakan -1 agar hasil dari like tidak bertambah oleh pemilik
         * sendiri.
         *
         *
         * Hal tersebut dapat menghindari like berulang dan pemilik forum tidak dapat memberikan like
         * pada forumnya sendiri
         *
         *
         * Catatan: like count tetap berada di forum. hal tersebut untuk mengurangi beban billing
         * karena jika dilakukan count akan membutuhkan banyak read operation dan resource
         */

        binding.artikelButtonLike.setOnClickListener {
            val forumRef = fireStore.document(navArgs.referenecePathDocumentString)
            val likeCount = navArgs.likeCount
            val dislikeCount = navArgs.dislikeCount
            val userId = auth.uid!!

            Toast.makeText(requireContext(), "Unimplemented Like Button", Toast.LENGTH_SHORT).show()
        }
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

    private fun setupRecyclerViewComment(komentarHubDocumentString: String?) {
        // TODO: setup recycler view comment

        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(
                requireContext(),
                "Unimplemented KomentarHubDocumentString",
                Toast.LENGTH_SHORT
            ).show()
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

    private suspend fun setupTextContent(
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