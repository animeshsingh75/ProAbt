package com.project.proabt.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
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
//val mFragmentList= arrayListOf<Fragment>()
//val mFragmentTitleList= arrayListOf<String>()
//override fun getCount(): Int {
//    return mFragmentList.size
//}
//
//override fun getItem(position: Int): Fragment {
//    return mFragmentList[position];
//}
//fun addFragment(fragment: Fragment,title:String){
//    mFragmentList.add(fragment)
//    mFragmentTitleList.add(title)
//}
//
//override fun getPageTitle(position: Int): CharSequence {
//    return mFragmentTitleList[position]
//}
