package com.gdsc.bingo.ui.komunitas

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.adapter.KomunitasTabAdapter
import com.gdsc.bingo.databinding.FragmentKomunitasBinding
import com.gdsc.bingo.model.Forums
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class KomunitasFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var komunitasViewModel: KomunitasViewModel

    val binding by lazy {
        FragmentKomunitasBinding.inflate(layoutInflater)
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
        setupViewPager()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setStatusAndBottomNavigation(this)


        setupSearchBar()

        setupCreateKomunitasReportFloatingActionButton()
        setupCreateKomunitasExtendedFloatingActionButton()
        setupCreateKomunitasReportFExtendedFloatingActionButton()
        komunitasViewModel.pullLatestData()


    }

    override fun onPause() {
        super.onPause()
        komunitasViewModel.deleteBeyondLimit()
    }

    private fun setupViewPager(isSaveEnabled : Boolean= true) {
        val viewPager2 = binding.komunitasViewPager
        val pagerAdapter = KomunitasTabAdapter(
            this
        )

        viewPager2.adapter = pagerAdapter

        viewPager2.isSaveEnabled = isSaveEnabled

        TabLayoutMediator(
            binding.komunitasTabLayout,
            viewPager2
        ) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }.attach()

        binding.komunitasTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    1 -> {
                        binding.komunitasExtendedFloatingActionButtonReport.apply {
                            show()
                            visibility = View.VISIBLE
                        }

                        binding.komunitasExtendedFloatingActionButton.apply {
                            hide()
                            visibility = View.GONE
                        }
                        binding.komunitasFloatingActionButtonReport.apply {
                            hide()
                            visibility = View.GONE
                        }
                    }
                    else -> {
                        binding.komunitasExtendedFloatingActionButtonReport.apply {
                            hide()
                            visibility = View.GONE
                        }

                        binding.komunitasExtendedFloatingActionButton.apply {
                            show()
                            visibility = View.VISIBLE
                        }
                        binding.komunitasFloatingActionButtonReport.apply {
                            show()
                            visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Handle tab unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle tab reselected
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("KomunitasFragment", "onDestroyView")
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

    }

    private fun setupEndOfList() {
        komunitasViewModel.loadEndOfList()
    }


    fun actionOpenDetail(forum: Forums) {
        val destination = with(forum){
            KomunitasFragmentDirections
                .actionKomunitasFragmentToArtikelFragment(
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

    fun actionVerticalButton(forum: Forums) {
        // TODO : navigate to detail forum
        Toast.makeText(requireContext(), "Vertical button on ${forum.title}", Toast.LENGTH_SHORT).show()
    }

    fun actionLike(forum: Forums) {
        // TODO : like forum
        Toast.makeText(requireContext(), "Like on ${forum.title}", Toast.LENGTH_SHORT).show()
    }

    fun actionComment(forum: Forums) {
        val destination = with(forum){
            KomunitasFragmentDirections
                .actionKomunitasFragmentToArtikelFragment(
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
                .actionNavigationKomunitasToFormPostActivity(Forums.ForumType.REPORT.fieldName)
            setOnClickListener {
                findNavController().navigate(destination)
            }
        }
    }

    private fun setupCreateKomunitasExtendedFloatingActionButton() {
        binding.komunitasExtendedFloatingActionButton.apply {
            hideIfUnauthenticated()

            val destination = KomunitasFragmentDirections
                .actionNavigationKomunitasToFormPostActivity(Forums.ForumType.ARTICLE.fieldName)

            setOnClickListener {
                findNavController().navigate(destination)
            }
        }
    }

    private fun setupCreateKomunitasReportFExtendedFloatingActionButton() {
        binding.komunitasExtendedFloatingActionButtonReport.apply {
            hideIfUnauthenticated()

            setOnClickListener {
                binding.komunitasFloatingActionButtonReport.performClick()
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

    fun setupPaginationAndScrollingBehaviour(
        recyclerView: RecyclerView,
        viewModel: KomunitasViewModel,
        forumType: Forums.ForumType
    ) {

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (auth.currentUser != null) {
                    if (dy > 20) {
                        if (forumType == Forums.ForumType.REPORT) {
                            binding.komunitasExtendedFloatingActionButtonReport.shrink()
                        } else {
                            binding.komunitasExtendedFloatingActionButton.shrink()
                            binding.komunitasFloatingActionButtonReport.hide()
                        }
                    } else if (dy < 0) {
                        if (forumType == Forums.ForumType.REPORT) {
                            binding.komunitasExtendedFloatingActionButtonReport.extend()
                        } else {
                            binding.komunitasExtendedFloatingActionButton.extend()
                            binding.komunitasFloatingActionButtonReport.show()
                        }
                    }
                } else {
                    binding.komunitasExtendedFloatingActionButton.hide()
                    binding.komunitasExtendedFloatingActionButtonReport.hide()
                    binding.komunitasFloatingActionButtonReport.hide()
                }




                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisiblePosition >= totalItemCount - 2) {
                    viewModel.loadMoreRecyclerData(forumType)
                }
            }
        })
    }

    fun actionOpenRoute(forum: Forums) {
        val destination = KomunitasFragmentDirections
            .actionNavigationKomunitasToPinPointActivity(
                "${forum.vicinity?.latitude!!}",
                "${forum.vicinity?.longitude!!}"
            )
        findNavController().navigate(destination)
    }

}