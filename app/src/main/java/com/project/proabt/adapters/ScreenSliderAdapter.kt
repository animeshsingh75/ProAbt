package com.project.proabt.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.proabt.fragments.InboxFragment
import com.project.proabt.fragments.PeopleFragment

class ScreenSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int=2

    override fun createFragment(position: Int): Fragment=when(position){
        0-> InboxFragment()
        else-> PeopleFragment()
    }

}
