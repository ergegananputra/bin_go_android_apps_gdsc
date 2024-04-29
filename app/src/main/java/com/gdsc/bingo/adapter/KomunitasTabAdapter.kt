package com.gdsc.bingo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gdsc.bingo.ui.komunitas.child.KomunitasEducationFragment
import com.gdsc.bingo.ui.komunitas.child.KomunitasPostFragment
import com.gdsc.bingo.ui.komunitas.child.KomunitasReportFragment
import com.gdsc.bingo.ui.komunitas.child.KomunitasTricksFragment

class KomunitasTabAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val pages = listOf(
        KomunitasPostFragment(),
        KomunitasReportFragment(),
        KomunitasTricksFragment(),
        KomunitasEducationFragment()
    )

    override fun getItemCount(): Int = pages.size


    override fun createFragment(position: Int): Fragment = pages[position]
    fun getPageTitle(position: Int): String {
        return when (position) {
            0 -> "Semua"
            1 -> "Bin-Report"
            2 -> "Bin-Tricks"
            3 -> "Bin-Learn"
            else -> ""
        }
    }
}