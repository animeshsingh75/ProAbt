package com.project.proabt

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.proabt.adapters.ScreenSliderAdapter
import com.project.proabt.databinding.ActivityMainBinding


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
                0 -> {
                    tab.text = "CHATS"
                }
                1 -> {
                    tab.text = "PEOPLE"
                }
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