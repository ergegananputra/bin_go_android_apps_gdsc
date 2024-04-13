package com.gdsc.bingo.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.adapter.ForumPostAdapter
import com.gdsc.bingo.databinding.FragmentBerandaBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.preferences.AppPreferences
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class BerandaFragment : Fragment() {
    private lateinit var komunitasViewModel: KomunitasViewModel

    private val binding by lazy {
        FragmentBerandaBinding.inflate(layoutInflater)
    }

    private val forumPostAdapter by lazy {
        ForumPostAdapter(
            context = requireActivity(),
            storage = FirebaseStorage.getInstance(),
            actionComment = { forum -> actionComment(forum) },
            actionLike = { forum -> actionLike(forum) },
            actionVerticalButton = { forum -> actionVerticalButton(forum) },
            actionOpenDetail = { forum -> actionOpenDetail(forum) }
        )
    }

    private fun actionOpenDetail(forum: Forums) {
        val destination = with(forum){
            BerandaFragmentDirections
                .actionNavigationBerandaToArtikelFragment(
                    referenecePathDocumentString = referencePath?.path!!,
                    title = title!!,
                    text = text,
                    isUsingTextFile = isUsingTextFile,
                    textFilePathDocumentString = textFilePath,
                    videoLink = videoLink,
                    likeCount = likeCount,
                    dislikeCount = dislikeCount,
                    likesReference = likesReference?.path!!,
                    commentCount = commentCount,
                    thumbnailPhotosUrl = thumbnailPhotosUrl,
                    authorDocumentString = author?.path!!,
                    komentarHubDocumentString = komentarHub?.path,
                    createAtSeconds = createdAt?.toDate()?.time ?: 0
                )
        }
        findNavController().navigate(destination)
    }

    private fun actionVerticalButton(forum: Forums) {
        // TODO : navigate to detail forum
        Toast.makeText(requireContext(), "Vertical button on ${forum.title}", Toast.LENGTH_SHORT).show()
    }

    private fun actionLike(forum: Forums) {
        // TODO : like forum
        Toast.makeText(requireContext(), "Like on ${forum.title}", Toast.LENGTH_SHORT).show()
    }

    private fun actionComment(forum: Forums) {
        // TODO : navigate to komentar fragment
        Toast.makeText(requireContext(), "Comment on ${forum.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        komunitasViewModel = ViewModelProvider(this)[KomunitasViewModel::class.java]
        setupRecyclerMostPopular()
        return binding.root
    }

    private fun setupRecyclerMostPopular() {
        binding.berandaRecyclerMostLikeKomunitas.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        }

        komunitasViewModel.forum.observe(viewLifecycleOwner) {
            forumPostAdapter.submitList(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setStatusAndBottomNavigation(this)

        setupGreetings()
        setupBinPoints()
        setupKomunitasBinGo()
        setupBerandaPinPoint()
        refreshRecyclerMostLikeData()
    }

    private fun setupGreetings() {
        val preferences = AppPreferences(requireContext())
        val name = preferences.userName.let {
            if (it.isBlank()) {
                null
            } else {
                "$it!"
            }
        } ?: return
        val prefixGreet = getString(R.string.beranda_prefix_greeting)
        val text = "$prefixGreet\n$name"
        binding.berandaTextViewGreeting.text = text
    }

    private fun refreshRecyclerMostLikeData() {
        komunitasViewModel.loadMostLikeData()
    }

    private fun setupBerandaPinPoint() {
        binding.berandaCardBerandaContainer.setOnClickListener {
            val destination = BerandaFragmentDirections.actionNavigationBerandaToPinPointActivity()
            findNavController().navigate(destination)
        }
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

        val fireStore = FirebaseFirestore.getInstance()
        val preferences = AppPreferences(requireContext())
        val uid = preferences.userId.also {
            if (it.isBlank()) {
                binding.include.componentCardBinPointsTextViewPoints.text = ""
                return
            }
        }
        val tempUser = User()
        fireStore.collection(tempUser.table)
            .document(uid)
            .get()
            .addOnSuccessListener {
                val user = tempUser.toModel(it)
                binding.include.componentCardBinPointsTextViewPoints.text = user.score.toString()
                preferences.score = user.score.toInt()
            }
            .addOnFailureListener {
                binding.include.componentCardBinPointsTextViewPoints.text = preferences.score.toString()
            }
    }

}