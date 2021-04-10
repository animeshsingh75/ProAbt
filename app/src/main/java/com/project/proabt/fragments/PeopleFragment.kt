package com.project.proabt.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioButton
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.proabt.*
import com.project.proabt.adapters.EmptyViewHolder
import com.project.proabt.adapters.UserViewHolder
import com.project.proabt.models.User
import com.project.proabt.utils.SearchViewModel


private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2
lateinit var mAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>
class PeopleFragment : Fragment() {
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("rating", Query.Direction.DESCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupAdapter()
        val view = inflater.inflate(R.layout.fragment_people, container, false)
        val btnAddFilter = view?.findViewById<FloatingActionButton>(R.id.btnAddFilter)
        btnAddFilter!!.setOnClickListener {
            showPopup(it)
        }
        return view
    }

    private fun showPopup(anchorView: View) {
        val layout = layoutInflater.inflate(R.layout.activity_filter, null)
        val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val popupWindow = PopupWindow(layout, 580, 900, true)
        popupWindow.isOutsideTouchable = false
        val closeBtn = layout.findViewById<ImageView>(R.id.closeBtn)
        val none = layout.findViewById<RadioButton>(R.id.none)
        val cPlus = layout.findViewById<RadioButton>(R.id.cPlus)
        val java = layout.findViewById<RadioButton>(R.id.java)
        val python = layout.findViewById<RadioButton>(R.id.python)
        val mlAi = layout.findViewById<RadioButton>(R.id.mlAi)
        val appDev = layout.findViewById<RadioButton>(R.id.appDev)
        val webDev = layout.findViewById<RadioButton>(R.id.webDev)
        closeBtn.setOnClickListener {
            initialAdapter()
            popupWindow.dismiss()
        }
        none.setOnClickListener {
            initialAdapter()
        }
        cPlus.setOnClickListener {
            changeAdapter("C++")
        }
        java.setOnClickListener {
            changeAdapter("Java")
        }
        python.setOnClickListener {
            changeAdapter("Python")
        }
        mlAi.setOnClickListener {
            changeAdapter("ML & AI")
        }
        webDev.setOnClickListener {
            changeAdapter("Web Dev")
        }
        appDev.setOnClickListener {
            changeAdapter("App Dev")
        }
        popupWindow.showAtLocation(layout, Gravity.BOTTOM or Gravity.RIGHT, 70, 240)
        popupWindow.isFocusable = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val myActionMenuItem = menu.findItem(R.id.search)
        val sv = SearchView((activity as MainActivity?)!!.supportActionBar!!.themedContext)
        MenuItemCompat.setShowAsAction(
            myActionMenuItem,
            MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        )
        MenuItemCompat.setActionView(myActionMenuItem, sv)
        sv.setIconifiedByDefault(true)
        val searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

                Log.d("SearchView", "Closed")
                return true
            }

        })
    }

    private fun changeAdapterQuery(query: String) {
        Log.d("SearchView",query.toUpperCase())
        val filterQuery = FirebaseFirestore.getInstance().collection("users").orderBy("upper_name")
            .startAt(query.toUpperCase())
            .endAt(query.toUpperCase() + "\uf8ff")
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(filterQuery, config, User::class.java)
            .build()
        mAdapter.updateOptions(options)
    }

    private fun changeAdapter(value: String) {
        val filterQuery = FirebaseFirestore.getInstance().collection("users")
            .whereArrayContains("skills", value)
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(filterQuery, config, User::class.java)
            .build()
        mAdapter.updateOptions(options)
    }

    private fun setupAdapter() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database, config, User::class.java)
            .build()
        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return when (viewType) {
                    NORMAL_VIEW_TYPE -> UserViewHolder(
                        layoutInflater.inflate(
                            R.layout.list_item_people,
                            parent,
                            false
                        )
                    )
                    else -> EmptyViewHolder(
                        layoutInflater.inflate(
                            R.layout.empty_view,
                            parent,
                            false
                        )
                    )
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if (auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                    }
                    LoadingState.LOADING_MORE -> {
                    }
                    LoadingState.LOADED -> {
                    }
                    LoadingState.FINISHED -> {
                    }
                    LoadingState.ERROR -> {
                    }
                }
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                model: User
            ) {
                if (holder is UserViewHolder) {
                    holder.bind(user = model) { name: String, photo: String, id: String ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra(UID, id)
                        intent.putExtra(NAME, name)
                        intent.putExtra(IMAGE, photo)
                        startActivity(intent)
                    }
                } else {

                }

            }

        }
    }
    fun initialAdapter() {
        val filterQuery = FirebaseFirestore.getInstance().collection("users")
            .orderBy("rating", Query.Direction.DESCENDING)
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()
        val options = FirestorePagingOptions.Builder<User>()
            .setQuery(filterQuery, config, User::class.java)
            .build()
        mAdapter.updateOptions(options)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        val searchViewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
        searchViewModel.getQuery()!!.observe(viewLifecycleOwner,
            { t ->
                if (t != null) {
                    changeAdapterQuery(t)
                }
            })
        super.onViewCreated(view, savedInstanceState)

    }
}

