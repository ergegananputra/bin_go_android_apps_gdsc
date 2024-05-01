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
import com.gdsc.bingo.adapter.ForumReportAdapter
import com.gdsc.bingo.databinding.FragmentKomunitasReportBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.ui.komunitas.KomunitasFragment
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel
import com.gdsc.bingo.ui.komunitas.KomunitasViewModel.Companion.toModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class KomunitasReportFragment: Fragment() {

    private val binding by lazy {
        FragmentKomunitasReportBinding.inflate(layoutInflater)
    }

    private lateinit var storage: FirebaseStorage
    private lateinit var komunitasViewModel: KomunitasViewModel

    private var isSearchMode : Boolean = false

    private val forumPostAdapter by lazy {
        val komunitasFragment = parentFragment as KomunitasFragment
        ForumReportAdapter(
            context = requireActivity(),
            storage = storage,
            actionVerticalButton = { forum -> komunitasFragment.actionVerticalButton(forum) },
            actionOpenDetail = { forum -> komunitasFragment.actionOpenDetail(forum) },
            actionOpenRoute = { forum -> komunitasFragment.actionOpenRoute(forum) },

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
            binding.komunitasRecyclerViewReport,
            komunitasViewModel,
            Forums.ForumType.REPORT
        )
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
                            Forums.ForumType.REPORT.fieldName
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
        binding.komunitasRecyclerViewReport.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                komunitasViewModel.forumsRealmReport.collect() { forumsRealms ->
                    if (isSearchMode) return@collect
                    binding.komunitasRecyclerProgressBarReport.visibility = View.GONE
                    forumPostAdapter.submitList(forumsRealms.toModel())
                }
            }
        }

    }

    private fun swipeRefresh() {
        binding.komunitasSwipeRefreshLayoutReport.setOnRefreshListener {
            komunitasViewModel.pullLatestData()
            binding.komunitasSwipeRefreshLayoutReport.isRefreshing = false
        }
    }

}