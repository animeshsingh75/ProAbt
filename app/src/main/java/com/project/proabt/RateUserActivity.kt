package com.project.proabt

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.project.proabt.databinding.ActivityRateUserBinding
import com.project.proabt.models.FriendRating
import com.project.proabt.models.Rating
import com.project.proabt.models.User

class RateUserActivity : AppCompatActivity() {
    lateinit var binding: ActivityRateUserBinding
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    lateinit var ratingMap: Rating
    lateinit var currentUser: User
    var avgRating: Float = 0F
    var indivRating: Int = 0
    var clicked: Boolean = false
    lateinit var friendRMap: FriendRating
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rateTv.text = "Pls rate $name out of 5 stars."
        binding.rate5.setOnClickListener {
            indivRating = 5
            clicked = true
            binding.rate1.setImageResource(R.drawable.ic_star_rated)
            binding.rate2.setImageResource(R.drawable.ic_star_rated)
            binding.rate3.setImageResource(R.drawable.ic_star_rated)
            binding.rate4.setImageResource(R.drawable.ic_star_rated)
            binding.rate5.setImageResource(R.drawable.ic_star_rated)
        }
        binding.rate4.setOnClickListener {
            indivRating = 4
            clicked = true
            binding.rate1.setImageResource(R.drawable.ic_star_rated)
            binding.rate2.setImageResource(R.drawable.ic_star_rated)
            binding.rate3.setImageResource(R.drawable.ic_star_rated)
            binding.rate4.setImageResource(R.drawable.ic_star_rated)
            binding.rate5.setImageResource(R.drawable.ic_star_unrated)
        }
        binding.rate3.setOnClickListener {
            indivRating = 3
            clicked = true
            binding.rate1.setImageResource(R.drawable.ic_star_rated)
            binding.rate2.setImageResource(R.drawable.ic_star_rated)
            binding.rate3.setImageResource(R.drawable.ic_star_rated)
            binding.rate4.setImageResource(R.drawable.ic_star_unrated)
            binding.rate5.setImageResource(R.drawable.ic_star_unrated)
        }
        binding.rate2.setOnClickListener {
            indivRating = 2
            clicked = true
            binding.rate1.setImageResource(R.drawable.ic_star_rated)
            binding.rate2.setImageResource(R.drawable.ic_star_rated)
            binding.rate3.setImageResource(R.drawable.ic_star_unrated)
            binding.rate4.setImageResource(R.drawable.ic_star_unrated)
            binding.rate5.setImageResource(R.drawable.ic_star_unrated)
        }
        binding.rate1.setOnClickListener {
            indivRating = 1
            clicked = true
            binding.rate1.setImageResource(R.drawable.ic_star_rated)
            binding.rate2.setImageResource(R.drawable.ic_star_unrated)
            binding.rate3.setImageResource(R.drawable.ic_star_unrated)
            binding.rate4.setImageResource(R.drawable.ic_star_unrated)
            binding.rate5.setImageResource(R.drawable.ic_star_unrated)
        }
        binding.submitBtn.setOnClickListener {
            db.getReference("rating").get()
                .addOnSuccessListener {
                    if (clicked) {
                        db.getReference("ratingUser").get()
                            .addOnSuccessListener {
                                if (it.child(friendId!!).exists()) {
                                    val rating = it.child(friendId!!).getValue(Rating::class.java)
                                    db.getReference("rating").get().addOnSuccessListener {
                                        if (it.child(friendId!!).child(mCurrentUid).exists()) {
                                            val friendRating =
                                                it.child(friendId!!).child(mCurrentUid)
                                                    .getValue(FriendRating::class.java)
                                            val currentTime = System.currentTimeMillis() / 1000
                                            val timeDifference = currentTime - friendRating!!.sentAt
                                            if (timeDifference > 5) {
                                                val newRating =
                                                    ((rating!!.avgRating * rating.totalPeople) - friendRating.indiRating + indivRating) / rating.totalPeople
                                                avgRating = newRating
                                                Log.d("Rating","$avgRating")
                                                friendRMap = FriendRating(currentTime, indivRating)
                                                db.reference.child("rating/${friendId}/${mCurrentUid}")
                                                    .setValue(friendRMap)
                                                ratingMap = Rating(rating.totalPeople, newRating)
                                                db.reference.child("ratingUser/${friendId}")
                                                    .setValue(ratingMap)
                                                pushRating(avgRating)
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "You cannot the same user before 24 hrs",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } else {
                                            val currentTime = System.currentTimeMillis() / 1000
                                            val newRating =
                                                ((rating!!.avgRating * rating.totalPeople) + indivRating) / (rating.totalPeople + 1)
                                            avgRating = newRating
                                            Log.d("Rating","$avgRating")
                                            friendRMap = FriendRating(currentTime, indivRating)
                                            db.reference.child("rating/${friendId}/${mCurrentUid}")
                                                .setValue(friendRMap)
                                            ratingMap = Rating(rating.totalPeople + 1, newRating)
                                            db.reference.child("ratingUser/${friendId}")
                                                .setValue(ratingMap)
                                            pushRating(avgRating)
                                        }
                                    }
                                } else {
                                    val currentTime = System.currentTimeMillis() / 1000
                                    friendRMap = FriendRating(currentTime, indivRating)
                                    db.reference.child("rating/${friendId}/${mCurrentUid}")
                                        .setValue(friendRMap)
                                    ratingMap = Rating(1, indivRating.toFloat())
                                    avgRating = indivRating.toFloat()
                                    Log.d("Rating","$avgRating")
                                    db.reference.child("ratingUser/${friendId}").setValue(ratingMap)
                                    pushRating(avgRating)
                                }
                            }

                    } else {
                        Toast.makeText(this, "Please select a rating", Toast.LENGTH_LONG).show()
                    }
                }
        }
        binding.closeBtn.setOnClickListener {
            finish()
        }
    }
    private fun pushRating(avgRating:Float){
        FirebaseFirestore.getInstance().collection("users").document(friendId!!)
            .get()
            .addOnSuccessListener {
                Log.d("Rating",avgRating.toString())
                currentUser = it.toObject(User::class.java)!!
                val user = User(
                    currentUser.name,
                    currentUser.imageUrl,
                    currentUser.thumbImage,
                    currentUser.uid,
                    currentUser.deviceToken,
                    rating = avgRating,
                    skills = currentUser.skills
                )
                database.collection("users").document(friendId!!).set(user)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Your rating has been recorded",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
            }
    }

}

