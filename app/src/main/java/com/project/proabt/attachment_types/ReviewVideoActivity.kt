package com.project.proabt.attachment_types

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.proabt.*
import com.project.proabt.databinding.ActivityReviewVideoBinding
import com.project.proabt.models.Inbox
import com.project.proabt.models.Message
import com.project.proabt.models.User
import com.squareup.picasso.Picasso

class ReviewVideoActivity : AppCompatActivity() {
    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }
    private val sentvideo by lazy {
        Uri.parse(intent.getStringExtra("SENTVIDEO"))
    }
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    private val getUser by lazy {
        FirebaseFirestore.getInstance().document("users/$mCurrentUid")
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    lateinit var binding: ActivityReviewVideoBinding
    private lateinit var progressDialog: ProgressDialog
    lateinit var downloadUrl: String
    lateinit var msgMap: Message
    lateinit var currentUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = createProgressDialog("Getting the video. Please wait", false)
        progressDialog.show()
        uploadVideo(sentvideo)
        Log.d("PhotoUrifromReviw", sentvideo.toString())
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            startActivity(intent)
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
        binding.btnSend.setOnClickListener {
            Log.d("ClickedFl", "Clicked")
            sendMessage(downloadUrl)
            progressDialog = createProgressDialog("Sending the video. Please wait", false)
            progressDialog.show()
        }
        binding.nameTv.text = name
        Picasso.get()
            .load(image)
            .placeholder(R.drawable.defaultavatar)
            .error(R.drawable.defaultavatar)
            .into(binding.userImgView)
    }

    private fun uploadVideo(it: Uri) {
        val ref = storage.reference.child("uploads/video/" + System.currentTimeMillis())
        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                val mediaController= MediaController(this)
                mediaController.setAnchorView(binding.sentVideo)
                binding.sentVideo.setMediaController(mediaController)
                Log.d("TaskResult",task.result.toString())
                Log.d("TaskResult",downloadUrl)
                binding.sentVideo.setVideoURI(task.result)
                binding.sentVideo.requestFocus()
                binding.sentVideo.setOnPreparedListener {
                    progressDialog.dismiss()
                    binding.sentVideo.start()
                }
            }
        }.addOnFailureListener {

        }
    }

    private fun sendMessage(msgUrl: String) {
        val id = getMessages(friendId!!).push().key
        checkNotNull(id) { "Cannot by null" }
        getUser.get().addOnSuccessListener {
            val imageUrl = it.get("imageUrl") as String
            val senderName = it.get("name") as String
            msgMap =
                Message(msgUrl, mCurrentUid, id, imageUrl, senderName, "VIDEO")
            Log.d("msgMap", msgMap.imageUrl)
            getMessages(friendId!!).child(id).setValue(msgMap).addOnSuccessListener {
                Log.d("CHATS", "Completed")
            }.addOnFailureListener {
                Log.d("CHATS", it.localizedMessage)
            }
            updateLastMessage(msgMap)
        }
    }

    private fun updateLastMessage(message: Message) {
        val inboxMap = Inbox(
            message.msg,
            friendId!!,
            mCurrentUid,
            name!!,
            name!!.toUpperCase(),
            image!!,
            count = 0,
            type = "VIDEO"
        )
        getInbox(mCurrentUid, friendId!!).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId!!, mCurrentUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImage
                        count = 1
                    }
                    value?.let {
                        if (it.from == message.senderId) {
                            inboxMap.count = value.count + 1
                        }
                    }
                    getInbox(friendId!!, mCurrentUid).setValue(inboxMap)
                    val intent = Intent(this@ReviewVideoActivity, ChatActivity::class.java)
                    intent.putExtra(UID, friendId)
                    intent.putExtra(NAME, name)
                    intent.putExtra(IMAGE, image)
                    if (::progressDialog.isInitialized)
                        progressDialog.dismiss()
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid

        }
    }

    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
            setMessage(message)
        }
    }
    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")

    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")
}