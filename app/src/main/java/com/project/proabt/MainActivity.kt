package com.project.proabt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.proabt.adapters.ScreenSliderAdapter
import com.project.proabt.databinding.ActivityMainBinding
import com.project.proabt.fragments.InboxFragment
import com.project.proabt.fragments.PeopleFragment


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.viewPager.adapter = ScreenSliderAdapter(this)
        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab: TabLayout.Tab, pos: Int ->
            when (pos) {
                0 -> tab.text = "CHATS"
                1 -> tab.text = "PEOPLE"
            }
        }.attach()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.search -> {
                Toast.makeText(this, "Action Search", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
//setUpWithViewPager(binding.viewPager)
//binding.tabs.setupWithViewPager(binding.viewPager)
//binding.viewPager.currentItem = 0
//binding.btnAddPerson.bringToFront()
//binding.btnAddPerson.setOnClickListener {
//    Log.wtf("Button", "ButtonClicked${ binding.viewPager.currentItem}")
//    binding.viewPager.setCurrentItem(1)
//    Log.wtf("Button", "ButtonClicked${ binding.viewPager.currentItem}")
//}
//binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//    override fun onPageScrolled(
//        position: Int,
//        positionOffset: Float,
//        positionOffsetPixels: Int
//    ) {
//    }
//
//    override fun onPageSelected(position: Int) {
//        when (position) {
//            0 -> {
//                binding.btnAddPerson.show()
//                binding.btnAddPerson.setOnClickListener {
//                    Log.wtf("Button", "ButtonClicked${ binding.viewPager.currentItem}")
//                    binding.viewPager.setCurrentItem(1)
//                    Log.wtf("Button", "ButtonClicked${ binding.viewPager.currentItem}")
//                }
//            }
//            1 -> {
//                binding.btnAddPerson.hide()
//            }
//        }
//    }
//
//    override fun onPageScrollStateChanged(state: Int) {}
//})

//fun setUpWithViewPager(viewPager: ViewPager) {
//    val adapter = ScreenSliderAdapter(supportFragmentManager)
//    adapter.addFragment(InboxFragment(), "Chats")
//    adapter.addFragment(PeopleFragment(), "People")
//    binding.viewPager.adapter = adapter
//}
//
