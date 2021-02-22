package com.example.whatsappclone

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsappclone.databinding.ActivityChatBinding
import com.example.whatsappclone.models.Message
import com.example.whatsappclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider

const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    lateinit var currentUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
        binding.nameTv.text = name
        Picasso.get().load(image).into(binding.userImgView)
        binding.sendBtn.setOnClickListener {
            binding.msgEdTv.text?.let {
                if(!it.isEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
    }

    private fun sendMessage(msg: String) {
        val id=getMessages(friendId!!).push().key
        checkNotNull(id){"Cannot by null"}
        val msgMap=Message(msg,mCurrentUid,id)
        getMessages(friendId!!).child(id).setValue(msgMap).addOnSuccessListener {
            Log.i("CHATS","Completed")
        }.addOnFailureListener {
            Log.i("CHATS",it.localizedMessage)
        }
    }

    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }
    private fun getMessages(friendId: String) =db.reference.child("messages/${getId(friendId)}")
    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")
}