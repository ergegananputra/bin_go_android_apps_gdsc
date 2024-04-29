package com.gdsc.bingo.ui.komunitas.child

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.bingo.adapter.ForumPostAdapter
import com.gdsc.bingo.databinding.FragmentKomunitasPostBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.ui.komunitas.KomunitasFragment
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel.Companion.toModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class KomunitasPostFragment : Fragment() {

    private val binding by lazy {
        FragmentKomunitasPostBinding.inflate(layoutInflater)
    }

    private val komunitasFragmentBinding by lazy {
        parentFragment as KomunitasFragment
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var komunitasViewModel: KomunitasViewModel

    private var isSearchMode : Boolean = false

    private val forumPostAdapter by lazy {
        val komunitasFragment = parentFragment as KomunitasFragment
        ForumPostAdapter(
            context = requireActivity(),
            storage = storage,
            actionComment = { forum -> komunitasFragment.actionComment(forum) },
            actionLike = { forum -> komunitasFragment.actionLike(forum) },
            actionVerticalButton = { forum -> komunitasFragment.actionVerticalButton(forum) },
            actionOpenDetail = { forum -> komunitasFragment.actionOpenDetail(forum) }
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
        setupEndOfList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh()
        setupRecyclerView()
        setupSearch()
        setupPaginationAndScrollingBehaviour()
    }

    private fun setupPaginationAndScrollingBehaviour() {
        (parentFragment as KomunitasFragment).setupPaginationAndScrollingBehaviour(
            binding.komunitasRecyclerViewPost,
            komunitasViewModel,
            Forums.ForumType.ARTICLE
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("KomunitasPostFragment", "onDestroyView")
    }

    private fun setupSearch() {
        val komunitasFragment = parentFragment as KomunitasFragment
        komunitasFragment.binding.komunitasTextInputLayoutSearch.editText?.doAfterTextChanged { query ->
            if (query.toString().isNotEmpty()) {
                lifecycleScope.launch {
                    isSearchMode = true

                    val result = komunitasViewModel
                        .searchForums(
                            query.toString(),
                            Forums.ForumType.ARTICLE.fieldName
                        )

                    forumPostAdapter.submitList(result)
                }
            } else {
                isSearchMode = false

                komunitasViewModel.pullLatestData()
            }
        }
    }

    private fun setupEndOfList() {
        komunitasViewModel.loadEndOfList()
    }


    private fun setupRecyclerView() {
        binding.komunitasRecyclerViewPost.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                komunitasViewModel.forumsRealm.collect() { forumsRealms ->
                    if (isSearchMode) return@collect
                    binding.komunitasRecyclerProgressBarPost.visibility = View.GONE
                    forumPostAdapter.submitList(forumsRealms.toModel())
                }
            }
        }

    }

    private fun swipeRefresh() {
        binding.komunitasSwipeRefreshLayoutPost.setOnRefreshListener {
            komunitasViewModel.pullLatestData()
            binding.komunitasSwipeRefreshLayoutPost.isRefreshing = false
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = KomunitasPostFragment()
    }

}