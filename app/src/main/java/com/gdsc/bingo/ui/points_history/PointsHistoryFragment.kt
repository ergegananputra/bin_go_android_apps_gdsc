package com.gdsc.bingo.ui.points_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.bingo.MainActivity
import com.gdsc.bingo.adapter.RankAdapter
import com.gdsc.bingo.databinding.FragmentPointsHistoryBinding
import com.gdsc.bingo.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source


class PointsHistoryFragment : Fragment() {
    private lateinit var fireStore : FirebaseFirestore

    private val binding by lazy {
        FragmentPointsHistoryBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        (activity as MainActivity).setStatusAndBottomNavigation(this@PointsHistoryFragment)


        fireStore = FirebaseFirestore.getInstance()
        return binding.root
    }

    private val rankAdapter : RankAdapter = RankAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackButton()
        setupRecyclerView()
        refreshRecyclerData()
    }

    private fun refreshRecyclerData() {
        val tempUser = User()
        fireStore.collection(tempUser.table)
            .orderBy(User.Keys.score, Query.Direction.DESCENDING)
            .get(Source.SERVER)
            .addOnSuccessListener {
                val lists = tempUser.toModels(it)
                rankAdapter.submitList(lists)
            }
    }

    private fun setupRecyclerView() {
        binding.pointsHistoryRecyclerView.apply {
            adapter = rankAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }



    private fun setupBackButton() {
        binding.pointsHistoryButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


}