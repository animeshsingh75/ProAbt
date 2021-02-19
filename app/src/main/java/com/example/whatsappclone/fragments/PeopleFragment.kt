package com.example.whatsappclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.adapters.UserViewHolder
import com.example.whatsappclone.models.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PeopleFragment : Fragment() {
    lateinit var mAdapter: FirestorePagingAdapter<User, UserViewHolder>
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.DESCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    private fun setupAdapter() {
        val config=PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()
        val options=FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database,config,User::class.java)
            .build()
        mAdapter=object :FirestorePagingAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view=layoutInflater.inflate(R.layout.list_item,parent,false)
                return UserViewHolder(view)
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when(state){
                    LoadingState.LOADING_INITIAL ->{}
                    LoadingState.LOADING_MORE ->{}
                    LoadingState.LOADED -> {}
                    LoadingState.FINISHED ->{}
                    LoadingState.ERROR -> {}
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.bind(user = model)
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView=view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply {
            layoutManager=LinearLayoutManager(requireContext())
            adapter=mAdapter
        }
        super.onViewCreated(view, savedInstanceState)

    }
}
