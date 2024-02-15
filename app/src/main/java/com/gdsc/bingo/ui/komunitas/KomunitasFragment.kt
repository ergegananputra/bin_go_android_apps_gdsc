package com.gdsc.bingo.ui.komunitas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.adapter.ForumPostAdapter
import com.gdsc.bingo.databinding.FragmentKomunitasBinding
import com.gdsc.bingo.model.Forums
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class KomunitasFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var komunitasViewModel: KomunitasViewModel

    private val binding by lazy {
        FragmentKomunitasBinding.inflate(layoutInflater)
    }

    private val forumPostAdapter by lazy {
        ForumPostAdapter(
            context = requireActivity(),
            storage = storage,
            actionComment = { forum -> actionComment(forum) },
            actionLike = { forum -> actionLike(forum) },
            actionVerticalButton = { forum -> actionVerticalButton(forum) },
            actionOpenDetail = { forum -> actionOpenDetail(forum) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        komunitasViewModel = ViewModelProvider(this)[KomunitasViewModel::class.java]
        setupRecyclerView()
        setupEndOfList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomNavigationVisibility(this)


        setupCreateKomunitasExtendedFloatingActionButton()
        komunitasViewModel.refreshRecyclerData()
        setupSwipeRefresh()
        setupPaginationAndScrollingBehaviour()
    }

    private fun setupEndOfList() {
        komunitasViewModel.loadEndOfList()
    }

    private fun setupPaginationAndScrollingBehaviour() {
        binding.komunitasRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    binding.komunitasExtendedFloatingActionButton.shrink()
                } else if (dy < 0) {
                    binding.komunitasExtendedFloatingActionButton.extend()
                }


                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisiblePosition >= totalItemCount - 2) {
                    komunitasViewModel.loadMoreRecyclerData()
                }
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.komunitasSwipeRefreshLayout.setOnRefreshListener {
            komunitasViewModel.refreshRecyclerData()
            binding.komunitasSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        binding.komunitasRecyclerView.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        komunitasViewModel.forum.observe(viewLifecycleOwner) {
            binding.komunitasRecyclerProgressBar.visibility = View.GONE
            forumPostAdapter.submitList(it)
        }
    }



    private fun actionOpenDetail(forum: Forums) {
        val destination = with(forum){
            KomunitasFragmentDirections
                .actionNavigationKomunitasToArtikelFragment(
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

    private fun setupCreateKomunitasExtendedFloatingActionButton() {
        val destination = KomunitasFragmentDirections.actionNavigationKomunitasToFormPostFragment()
        binding.komunitasExtendedFloatingActionButton.setOnClickListener {
            findNavController().navigate(destination)
        }

        // TODO : Expand and collapse extended floating action button based on scroll in recycler view
    }



}