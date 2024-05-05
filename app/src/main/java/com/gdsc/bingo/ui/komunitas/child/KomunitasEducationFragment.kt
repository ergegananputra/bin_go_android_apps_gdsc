package com.gdsc.bingo.ui.komunitas.child

import android.os.Bundle
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
import com.gdsc.bingo.databinding.FragmentKomunitasEducationBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.ui.komunitas.KomunitasFragment
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel.Companion.toModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class KomunitasEducationFragment : Fragment() {

    private val binding by lazy {
        FragmentKomunitasEducationBinding.inflate(layoutInflater)
    }
    private val komunitasFragmentBinding by lazy {
        parentFragment as KomunitasFragment
    }

    private lateinit var storage: FirebaseStorage
    private lateinit var komunitasViewModel: KomunitasViewModel

    private var isSearchMode : Boolean = false

    private val forumPostAdapter by lazy {
        ForumPostAdapter(
            context = requireActivity(),
            storage = storage,
            actionComment = { forum -> komunitasFragmentBinding.actionComment(forum) },
            actionLike = { forum -> komunitasFragmentBinding.actionLike(forum) },
            actionVerticalButton = { forum -> komunitasFragmentBinding.actionVerticalButton(forum) },
            actionOpenDetail = { forum -> komunitasFragmentBinding.actionOpenDetail(forum) }

        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        storage = FirebaseStorage.getInstance()
        komunitasViewModel = ViewModelProvider(this)[KomunitasViewModel::class.java]
        setupEndOfList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        swipeRefresh()
        setupSearch()
        setupPaginationAndScrollingBehaviour()
    }

    private fun setupPaginationAndScrollingBehaviour() {
        (parentFragment as KomunitasFragment).setupPaginationAndScrollingBehaviour(
            binding.komunitasRecyclerViewEducation,
            komunitasViewModel,
            Forums.ForumType.WASTE_MANAGEMENT_EDUCATION
        )
    }

    private fun setupSearch() {
        komunitasFragmentBinding.binding.komunitasTextInputLayoutSearch.editText?.doAfterTextChanged { query ->
            if (query.toString().isNotEmpty()) {
                lifecycleScope.launch {
                    isSearchMode = true

                    val result = komunitasViewModel
                        .searchForums(
                            query.toString(),
                            Forums.ForumType.WASTE_MANAGEMENT_EDUCATION.fieldName
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
        binding.komunitasRecyclerViewEducation.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                komunitasViewModel.forumsRealmEducation.collect() { forumsRealms ->
                    if (isSearchMode) return@collect
                    binding.komunitasRecyclerProgressBarEducation.visibility = View.GONE
                    forumPostAdapter.submitList(forumsRealms.toModel())
                }
            }
        }

    }

    private fun swipeRefresh() {
        binding.komunitasSwipeRefreshLayoutEducation.setOnRefreshListener {
            komunitasViewModel.pullLatestData()
            binding.komunitasSwipeRefreshLayoutEducation.isRefreshing = false
        }
    }


}