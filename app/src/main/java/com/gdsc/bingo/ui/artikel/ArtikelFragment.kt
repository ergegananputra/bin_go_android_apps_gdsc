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
import com.gdsc.bingo.adapter.ImagePostAdapter
import com.gdsc.bingo.adapter.KomentarAdapter
import com.gdsc.bingo.databinding.FragmentArtikelBinding
import com.gdsc.bingo.model.FireModel
import com.gdsc.bingo.model.Forums.Keys.commentCount
import com.gdsc.bingo.model.Komentar
import com.gdsc.bingo.model.Like
import com.gdsc.bingo.model.PostImage
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.points.Points
import com.gdsc.bingo.services.points.PointsRewardSystem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId



class ArtikelFragment : Fragment(), PointsRewardSystem {
    private val navArgs by navArgs<ArtikelFragmentArgs>()

    private lateinit var fireStore : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage

    private val binding by lazy { FragmentArtikelBinding.inflate(layoutInflater) }

    private val komenAdapter = KomentarAdapter(
        storage = FirebaseStorage.getInstance()
    )

    private val imagePostAdapter = ImagePostAdapter(
        storage = FirebaseStorage.getInstance(),
        isOnline = true
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        setupShimmer()
        setupRecyclerViewComment()
        setupRecyclerPostImage()
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        return binding.root
    }

    private fun setupRecyclerPostImage() {
        binding.artikelImageViewContent.apply {
            adapter = imagePostAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        }
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
        (activity as MainActivity).setStatusAndBottomNavigation(this)

        setupToolbarButton()
        lifecycleScope.launch(Dispatchers.Main) {
            this.launch { setupTitle() }
            this.launch { setupProfile() }
            this.launch{ setupTimestamp(navArgs.createAtSeconds) }
            this.launch(Dispatchers.IO) { setupImagesContent(navArgs.referenecePathDocumentString) }
            this.launch { setupTextContent(navArgs.text, navArgs.isUsingTextFile, navArgs.textFilePathDocumentString) }
            this.launch { setupLikeAndCommentCount(navArgs.likeCount, navArgs.dislikeCount, navArgs.commentCount) }
            this.launch(Dispatchers.IO) { setupVideoContent(navArgs.videoLink) }
            this.launch(Dispatchers.IO) { loadRecyclerCommentData() }
            this.launch(Dispatchers.Main) { setupKomentar() }
            this.launch(Dispatchers.Main) { setupYoutubePlayerVideo() }
        }
        setupLikeButton()


    }

    private fun setupYoutubePlayerVideo() {
        val youtubePlayer = binding.artikelYoutubePlayer

        youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = navArgs.videoLink ?: run {
                    Log.e("ArtikelFragment", "Video link is null")
                    youtubePlayer.visibility = View.GONE
                    return
                }

                youtubePlayer.visibility = View.VISIBLE
                youTubePlayer.loadVideo(videoId, 0f)
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                Log.e("ArtikelFragment", "YoutubePlayer error: $error")
                youtubePlayer.visibility = View.GONE
            }
        })
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

                    commentPointRewards(
                        fireStore.collection(User().table).document(auth.uid!!),
                        fireStore.document(navArgs.authorDocumentString)
                    )

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

                                val newLike = if (initialState) likeCount else {
                                    likePointRewards(user, author)
                                    likeCount + 1
                                }

                                forumRef.update("like_count", newLike)



                                setupLikeAndCommentCount(newLike, navArgs.dislikeCount, navArgs.commentCount)
                            }
                        return@addOnSuccessListener
                    } else {
                        documentReference.reference.delete()
                            .addOnSuccessListener {

                                val newLike = if (initialState) {
                                    unlikePointRewards(user, author)
                                    likeCount - 1
                                } else likeCount

                                forumRef.update("like_count", newLike)



                                setupLikeAndCommentCount(newLike, navArgs.dislikeCount, navArgs.commentCount)
                            }
                    }


                }

        }

    }

    override fun likePointRewards(user: DocumentReference, author: DocumentReference) {
        author.get(Source.SERVER)
            .addOnSuccessListener { userDocument ->
                val newScore = userDocument.get(User.Keys.score) as Long + Points.RECEIVED_LIKE
                userDocument.reference.update(User.Keys.score, newScore)
            }

        user.get(Source.SERVER)
            .addOnSuccessListener { userDocument ->
                val newScore = userDocument.get(User.Keys.score) as Long + Points.DO_LIKE
                userDocument.reference.update(User.Keys.score, newScore)
            }
    }

    override fun unlikePointRewards(user: DocumentReference, author: DocumentReference) {
        author.get(Source.SERVER)
            .addOnSuccessListener { userDocument ->
                val newScore = (userDocument.get(User.Keys.score) as Long - Points.RECEIVED_LIKE).let {
                    if (it >= 0) it else 0
                }
                userDocument.reference.update(User.Keys.score, newScore)
            }

        user.get(Source.SERVER)
            .addOnSuccessListener { userDocument ->
                val newScore = (userDocument.get(User.Keys.score) as Long - Points.DO_LIKE).let {
                    if (it >= 0) it else 0
                }
                userDocument.reference.update(User.Keys.score, newScore)
            }
    }


    override fun commentPointRewards(user: DocumentReference, author: DocumentReference) {
        author.get(Source.SERVER)
            .addOnSuccessListener { userDocument ->
                val newScore = userDocument.get(User.Keys.score) as Long + Points.RECEIVED_COMMENT
                userDocument.reference.update(User.Keys.score, newScore)
            }

        user.get(Source.SERVER)
            .addOnSuccessListener { userDocument ->
                val newScore = userDocument.get(User.Keys.score) as Long + Points.DO_COMMENT
                userDocument.reference.update(User.Keys.score, newScore)
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

    private suspend fun setupImagesContent(documentReferenceString: String?) {
        withContext(Dispatchers.Main) {
            fireStore.document(documentReferenceString!!).collection(PostImage().table).get(Source.SERVER)
                .addOnSuccessListener { documentSnapshot ->
                    val imagePost = PostImage().toModels(documentSnapshot)
                    imagePostAdapter.submitList(imagePost)
                    binding.artikelShimmerImageContent.stop()
                }
                .addOnFailureListener { exception ->
                    Log.e("ArtikelFragment", "Error getting image content", exception)
                    binding.artikelImageViewContent.visibility = View.GONE
                    binding.artikelShimmerImageContent.stop()
                }

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