package com.project.proabt.setting

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.project.proabt.R
import com.project.proabt.adapters.RatingViewHolder
import com.project.proabt.databinding.ActivityShowRatingBinding
import com.project.proabt.models.FriendRating
import com.project.proabt.models.Rating
lateinit var mAdapter: FirebaseRecyclerAdapter<FriendRating, RatingViewHolder>
class ShowRatingActivity : AppCompatActivity() {
    lateinit var binding:ActivityShowRatingBinding
    lateinit var rating: Rating
    var exists=false
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityShowRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        db.getReference("ratingUser").child(mCurrentUid).get().addOnSuccessListener {

            if(it.exists()){
                exists=true
                binding.recyclerView.isVisible=true
                binding.avgRating.isVisible=true
                binding.profileSeparator.isVisible=true
                binding.noRatingTv.isVisible=false
                rating= it.getValue(Rating::class.java)!!
                Log.d("Rating",rating.avgRating.toString())
                val roundedOffRating=roundTo(rating.avgRating)
                binding.ratingTv.text=roundedOffRating.toString()
                setupAdapter()
                binding.recyclerView.apply {
                    layoutManager = LinearLayoutManager(this@ShowRatingActivity)
                    adapter = mAdapter
                }
            }
            else{
                exists=false
                binding.noRatingTv.isVisible=true
                binding.recyclerView.isVisible=false
                binding.avgRating.isVisible=false
                binding.profileSeparator.isVisible=false
            }
        }

    }
    private fun setupAdapter() {

        val baseQuery: Query =
            db.reference.child("rating").child(mCurrentUid)
        val options = FirebaseRecyclerOptions.Builder<FriendRating>()
            .setLifecycleOwner(this)
            .setQuery(baseQuery, FriendRating::class.java)
            .build()
        mAdapter = object : FirebaseRecyclerAdapter<FriendRating,RatingViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
                val inflater = layoutInflater
                return RatingViewHolder(inflater.inflate(R.layout.list_item_rating, parent, false))
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onBindViewHolder(
                viewHolder: RatingViewHolder,
                position: Int,
                rating: FriendRating
            ) {
                viewHolder.bind(rating)
            }
        }
    }
    fun roundTo(number:Float): Float = "%.1f".format(number).toFloat()
    override fun onStart() {
        super.onStart()
        if(exists==true){
            mAdapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if(exists==true){
            mAdapter.stopListening()
        }
    }
}