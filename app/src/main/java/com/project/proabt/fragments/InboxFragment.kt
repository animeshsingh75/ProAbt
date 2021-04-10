package com.project.proabt.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.project.proabt.ChatActivity
import com.project.proabt.R
import com.project.proabt.utils.SearchViewModel
import com.project.proabt.adapters.ChatViewHolder
import com.project.proabt.models.Inbox

lateinit var mAdapterInbox: FirebaseRecyclerAdapter<Inbox, ChatViewHolder>

class InboxFragment : Fragment() {
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val mDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewManager = LinearLayoutManager(requireContext())
        setupAdapter()
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)
        val btnAddPerson = view?.findViewById<FloatingActionButton>(R.id.btnAddPerson)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
        btnAddPerson!!.bringToFront()
        btnAddPerson.setOnClickListener {
            Log.d("Clicked", "Clicked")
            viewPager?.setCurrentItem(1, true)
        }
        return view
    }

    private fun setupAdapter() {

        val baseQuery: Query =
            mDatabase.reference.child("chats").child(auth.uid!!)

        val options = FirebaseRecyclerOptions.Builder<Inbox>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery, Inbox::class.java)
            .build()
        mAdapterInbox = object : FirebaseRecyclerAdapter<Inbox, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                val inflater = layoutInflater
                return ChatViewHolder(inflater.inflate(R.layout.list_item_inbox, parent, false))
            }

            override fun onBindViewHolder(
                viewHolder: ChatViewHolder,
                position: Int,
                inbox: Inbox
            ) {
                viewHolder.bind(inbox) { name: String, photo: String, id: String ->
                    Log.d("Image", "Item Image:$photo")
                    startActivity(
                        ChatActivity.createChatActivity(
                            requireContext(),
                            id,
                            name,
                            photo
                        )
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAdapterInbox.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapterInbox.stopListening()
    }

    fun changeQuery(query: String) {
        val baseQuery: Query =
            mDatabase.reference.child("chats").child(auth.uid!!).orderByChild("upper_name").startAt(query.toUpperCase())
                .endAt(query.toUpperCase() + "\uf8ff")
        val options = FirebaseRecyclerOptions.Builder<Inbox>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery, Inbox::class.java)
            .build()
        mAdapterInbox.updateOptions(options)
    }

    fun initialQuery() {

        val baseQuery: Query =
            mDatabase.reference.child("chats").child(auth.uid!!)
        val options = FirebaseRecyclerOptions.Builder<Inbox>()
            .setQuery(baseQuery, Inbox::class.java)
            .build()
        mAdapterInbox.updateOptions(options)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = mAdapterInbox
        }
        val searchViewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
        searchViewModel.getQuery()!!.observe(viewLifecycleOwner,
            { t ->
                if (t != null) {
                    changeQuery(t)
                }
            })
    }

}