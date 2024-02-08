package com.gdsc.bingo.ui.komunitas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.adapter.ForumPostAdapter
import com.gdsc.bingo.databinding.FragmentKomunitasBinding
import com.gdsc.bingo.model.Forums
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage

class KomunitasFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

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
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomNavigationVisibility(this)


        setupCreateKomunitasExtendedFloatingActionButton()
        refreshRecyclerData(true)

    }

    private fun setupRecyclerView() {
        binding.komunitasRecyclerView.apply {
            adapter = forumPostAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun refreshRecyclerData(forceUpdate : Boolean = false) {
        val source = if (forceUpdate) Source.SERVER else Source.DEFAULT
        val tempObj = Forums()

        firestore.collection(tempObj.table).get(source)
            .addOnSuccessListener { result ->
                val forums = tempObj.toModel(result)
                forumPostAdapter.submitList(forums)
                Log.i("KomunitasFragment", "Data refreshed with ${forums.size} items")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actionOpenDetail(forum: Forums) {
        // TODO : navigate to detail forum
        Toast.makeText(requireContext(), "Open detail on ${forum.title}", Toast.LENGTH_SHORT).show()
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