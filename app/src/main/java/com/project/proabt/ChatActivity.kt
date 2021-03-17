package com.project.proabt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.proabt.adapters.ChatAdapter
import com.project.proabt.databinding.ActivityChatBinding
import com.project.proabt.models.*
import com.project.proabt.utils.KeyboardVisibilityUtil
import com.project.proabt.utils.isSameDayAs
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"
const val SENTPHOTO = "sentphoto"

class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    lateinit var msgMap: Message
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
    private val getUser by lazy {
        FirebaseFirestore.getInstance().document("users/$mCurrentUid")
    }
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private var backclicked: Boolean = false
    private var attachmentclick: Boolean = false
    private val messages = mutableListOf<ChatEvent>()
    lateinit var chatAdapter: ChatAdapter
    lateinit var currentUser: User
    lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keyboardVisibilityHelper = KeyboardVisibilityUtil(binding.rootView) {
            binding.msgRv.scrollToPosition(messages.size - 1)
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            checkInitialMessage()
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
        chatAdapter = ChatAdapter(messages, mCurrentUid)
        binding.msgRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        binding.nameTv.text = name
        Picasso.get().load(image)
            .into(binding.userImgView)
        val emojiPopup = EmojiPopup.Builder.fromRootView(binding.rootView).build(binding.msgEdTv)
        binding.smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
        binding.attachment.setOnClickListener {
            binding.attachment.bringToFront()
            Log.d("Clicked", "Clicked")
            binding.layoutActionsContainer.isVisible = !attachmentclick
            attachmentclick = !attachmentclick
        }
        binding.btnGalleryButton.setOnClickListener {
            Log.d("Gallery", "Clicked")
            pickImageFromGallery()
        }
        binding.btnGallery.setOnClickListener {
            Log.d("Gallery", "Clicked")
            pickImageFromGallery()
        }
        binding.swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                binding.swipeToLoad.isRefreshing = false
            }
        }
        listenToMessages() { msg, update ->
            if (update) {
                updateMessage(msg)
            } else {
                addMessage(msg)
            }
        }
        binding.sendBtn.setOnClickListener {
            binding.msgEdTv.text?.let {
                if (!it.isEmpty()) {
                    backclicked = true
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }
        markAsRead()
    }

    override fun onBackPressed() {
        checkInitialMessage()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            val imageUri = data?.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            } catch (e: IOException) {
                Log.e("IOException", e.toString())
            }
            Log.d("SENTPHOTO", imageUri.toString())
            val picturePath = getPath(this, imageUri)
            Log.d("Picture Path", picturePath!!)
            val intent = Intent(this, ReviewImageActivity::class.java)
            intent.putExtra("PicturePath", picturePath)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            intent.putExtra(SENTPHOTO, imageUri.toString())
            startActivity(intent)
        }
    }
    private fun markAsRead() {
        getInbox(mCurrentUid, friendId!!).child("count").setValue(0)
    }

    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId!!).child(id).updateChildren(mapOf("liked" to status))
    }

    private fun listenToMessages(addMessage: (msg: Message, update: Boolean) -> Unit) {
        getMessages(friendId!!)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.getValue(Message::class.java)!!
                    addMessage(msg, false)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.getValue(Message::class.java)!!
                    addMessage(msg, true)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun addMessage(msg: Message) {
        val eventBefore = messages.lastOrNull()
        if ((eventBefore != null && !eventBefore.sentAt.isSameDayAs(msg.sentAt)) || eventBefore == null) {
            messages.add(
                DateHeader(
                    msg.sentAt, context = this
                )
            )
        }
        messages.add(msg)
        chatAdapter.notifyItemInserted(messages.size)
        binding.msgRv.smoothScrollToPosition(messages.size + 1)
    }

    private fun updateMessage(msg: Message) {
        val position = messages.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        messages[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId!!).push().key
        checkNotNull(id) { "Cannot by null" }
        getUser.get().addOnSuccessListener {
            val imageUrl = it.get("imageUrl") as String
            val senderName = it.get("name") as String
            msgMap = Message(msg, mCurrentUid, id, imageUrl, senderName, "TEXT")
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
            name!!,
            image!!,
            count = 0
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
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }

    private fun checkInitialMessage() {
        val reference = db.getReference("chats").child(mCurrentUid).child(friendId!!).get()
            .addOnSuccessListener {
                val result = !(it.child("name").exists() || backclicked)
                Log.d("ErrorDB", "$result")
                if (result) {
                    val inboxMap = Inbox(
                        "",
                        friendId!!,
                        name!!,
                        image!!,
                        count = 0
                    )
                    getInbox(mCurrentUid, friendId!!).setValue(inboxMap).addOnCompleteListener {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
    }
    fun getPath(context: Context, uri: Uri?): String? {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri!!, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index: Int = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid

        }
    }

    override fun onResume() {
        super.onResume()
        binding.rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        binding.rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")
    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")

    companion object {
        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(UID, id)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            return intent
        }
    }

}