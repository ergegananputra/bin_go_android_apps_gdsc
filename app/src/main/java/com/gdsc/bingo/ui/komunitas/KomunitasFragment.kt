package com.gdsc.bingo.ui.komunitas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.adapter.ForumPostAdapter
import com.gdsc.bingo.databinding.FragmentKomunitasBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel.Companion.toModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

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
        (activity as MainActivity).setStatusAndBottomNavigation(this)

        setupSearchBar()

        setupCreateKomunitasReportFloatingActionButton()
        setupCreateKomunitasExtendedFloatingActionButton()
        komunitasViewModel.pullLatestData()
        setupSwipeRefresh()
        setupPaginationAndScrollingBehaviour()
    }

    private fun setupSearchBar() {
        binding.komunitasOpenSearchButton.visibility = View.VISIBLE
        binding.komunitasTextViewTitle.visibility = View.VISIBLE
        binding.komunitasTextInputLayoutSearch.visibility = View.GONE

        binding.komunitasOpenSearchButton.setOnClickListener { button ->
            button.visibility = if (button.isVisible) {
                binding.komunitasTextInputLayoutSearch.apply {
                    visibility = View.VISIBLE
                    this.requestFocus()
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(this.editText, InputMethodManager.SHOW_IMPLICIT)
                }
                View.GONE
            } else {
                binding.komunitasTextInputLayoutSearch.apply {
                    visibility = View.GONE
                    editText?.text?.clear()
                    clearFocus()
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.editText?.windowToken, 0)
                }
                View.VISIBLE
            }

            binding.komunitasTextViewTitle.visibility = button.visibility
        }

        binding.komunitasTextInputLayoutSearch.setEndIconOnClickListener {
            val query = binding.komunitasTextInputLayoutSearch.editText?.text.toString().trim()
            if (query.isNotBlank()) {
                binding.komunitasTextInputLayoutSearch.editText?.text?.clear()
            } else {
                binding.komunitasOpenSearchButton.performClick()
            }
        }

        binding.komunitasTextInputLayoutSearch.editText?.doAfterTextChanged {
            val query = it.toString().trim()
            if (query.isNotBlank()) {
                // TODO : search for query
                Toast.makeText(requireContext(), "Search for $query", Toast.LENGTH_SHORT).show()
            } else {
                // TODO : search for all
                Toast.makeText(requireContext(), "Search for all", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupEndOfList() {
        komunitasViewModel.loadEndOfList()
    }

    private fun setupPaginationAndScrollingBehaviour() {
        binding.komunitasRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 20) {
                    binding.komunitasExtendedFloatingActionButton.shrink()
                    binding.komunitasFloatingActionButtonReport.hide()
                } else if (dy < 0) {
                    binding.komunitasExtendedFloatingActionButton.extend()
                    binding.komunitasFloatingActionButtonReport.show()
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
//            komunitasViewModel.refreshRecyclerData()
            komunitasViewModel.pullLatestData()
            binding.komunitasSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        binding.komunitasRecyclerView.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                komunitasViewModel.forumsRealm.collect() { forumsRealms ->
                    binding.komunitasRecyclerProgressBar.visibility = View.GONE
                    forumPostAdapter.submitList(forumsRealms.toModel())
                }
            }
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

    private fun setupCreateKomunitasReportFloatingActionButton() {
        binding.komunitasFloatingActionButtonReport.apply {
            hideIfUnauthenticated()

            val destination = KomunitasFragmentDirections
                .actionNavigationKomunitasToFormPostFragment(Forums.ForumType.REPORT.fieldName)
            setOnClickListener {
                findNavController().navigate(destination)
            }
        }
    }

    private fun setupCreateKomunitasExtendedFloatingActionButton() {
        binding.komunitasExtendedFloatingActionButton.apply {
            hideIfUnauthenticated()

            val destination = KomunitasFragmentDirections
                .actionNavigationKomunitasToFormPostFragment(Forums.ForumType.ARTICLE.fieldName)

            setOnClickListener {
                findNavController().navigate(destination)
            }
        }
    }

    private fun View.hideIfUnauthenticated() {
        visibility = if (auth.currentUser == null) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

}