package com.project.proabt

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.proabt.adapters.ScreenSliderAdapter
import com.project.proabt.databinding.ActivityMainBinding
import com.project.proabt.fragments.InboxFragment
import com.project.proabt.fragments.PeopleFragment
import com.project.proabt.utils.SearchViewModel


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var searchView: SearchView
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu!!.add(
            0, 1, 1, menuIconWithText(
                getResources().getDrawable(R.drawable.ic_settings), resources.getString(
                    R.string.settings
                )
            )
        )
        val myActionMenuItem = menu!!.findItem(R.id.search)
        searchView = myActionMenuItem!!.actionView as SearchView
        searchView.setIconifiedByDefault(true)
        searchView.isIconified = true
        val searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        searchView.setOnQueryTextFocusChangeListener { _, newViewFocus ->
            if (!newViewFocus) {
                if (binding.viewPager.currentItem == 1) {
                    val peopleFragment = PeopleFragment()
                    peopleFragment.initialAdapter()
                } else {
                    val inboxFragment = InboxFragment()
                    inboxFragment.initialQuery()
                }
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.setQuery(newText!!)
                return false
            }
        })
        myActionMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                if (binding.viewPager.currentItem == 1) {
                    val peopleFragment = PeopleFragment()
                    peopleFragment.initialAdapter()
                } else {
                    val inboxFragment = InboxFragment()
                    inboxFragment.initialQuery()
                }
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun displayToast(message: String) {
        Toast.makeText(
            applicationContext, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun menuIconWithText(r: Drawable, title: String): CharSequence? {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("    $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

}