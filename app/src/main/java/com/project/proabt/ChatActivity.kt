package com.project.proabt

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.proabt.adapters.ChatAdapter
import com.project.proabt.attachment_types.CameraActivity
import com.project.proabt.attachment_types.ReviewImageActivity
import com.project.proabt.databinding.ActivityChatBinding
import com.project.proabt.models.*
import com.project.proabt.utils.KeyboardVisibilityUtil
import com.project.proabt.utils.PathUtils
import com.project.proabt.utils.formateMilliSeccond
import com.project.proabt.utils.isSameDayAs
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

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
    private lateinit var progressDialog: ProgressDialog
    lateinit var downloadUrl: String
    private val messages = mutableListOf<ChatEvent>()
    lateinit var chatAdapter: ChatAdapter
    lateinit var currentUser: User
    lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId

        when(id){
            R.id.rateUser->{
                val intent=Intent(this,RateUserActivity::class.java)
                intent.putExtra(NAME,name)
                intent.putExtra(UID,friendId)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keyboardVisibilityHelper = KeyboardVisibilityUtil(binding.rootView) {
            binding.msgRv.scrollToPosition(messages.size - 1)
        }
        Log.d("Messages","${messages.count()}")
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
            pickImageFromGallery()
        }
        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }
        binding.btnAudioButton.setOnClickListener {
            pickAudio()
        }
        binding.btnAudio.setOnClickListener {
            pickAudio()
        }

        binding.btnCameraX.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            startActivity(intent)
        }
        binding.btnCameraXButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            startActivity(intent)
        }
        binding.btnDocButton.setOnClickListener {
            pickDocument()
        }
        binding.btnDoc.setOnClickListener {
            pickDocument()
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
                    sendMessage(it.toString(), "TEXT")
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

    private fun pickAudio() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(
            intent,
            1002
        )
    }

    private fun pickDocument() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            val imageUri = data?.data
            Log.wtf("SENTPHOTO", imageUri.toString())
            val picturePath = PathUtils.getPath(imageUri!!, this)
            Log.wtf("SENTPHOTO", picturePath!!)
            val intent = Intent(this, ReviewImageActivity::class.java)
            intent.putExtra("PicturePath", picturePath)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            intent.putExtra("SENTPHOTO", imageUri.toString())
            intent.putExtra("SENTFROM", "CHAT")
            startActivity(intent)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
            val pdfuri = data?.data
            progressDialog = createProgressDialog("Sending a PDF. Please wait", false)
            progressDialog.show()
            Log.d("PDFURI", "Progress Dialog")
            Log.d("PDFURI", pdfuri.toString())
            val name = PathUtils.getFileName(this, pdfuri!!)
            Log.d("PDFURI", name)
            uploadDoc(pdfuri, name)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 1002) {
            val audiouri = data?.data
            progressDialog = createProgressDialog("Sending a Audio. Please wait", false)
            progressDialog.show()
            Log.d("AUDIOURI", audiouri.toString())
            val audioPath = PathUtils.getPath(audiouri!!, this)
            Log.d("AUDIOURI", audioPath!!)
            val duration = getDuration(audioPath)
            Log.d("AUDIOURI", duration.toString())
            uploadAudio(audiouri, duration.toString())
        }
    }


    private fun uploadDoc(it: Uri, name: String) {
        val ref = storage.reference.child("uploads/doc/" + System.currentTimeMillis())
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
                sendMessage(downloadUrl, "DOC", name)

            }
        }.addOnFailureListener {

        }
    }

    private fun uploadAudio(it: Uri, duration: String) {
        val ref = storage.reference.child("uploads/audio/" + System.currentTimeMillis())
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
                sendMessage(downloadUrl, "AUDIO", duration = duration)
            }
        }.addOnFailureListener {

        }
    }

    private fun getDuration(path: String): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(path)
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return formateMilliSeccond(durationStr!!.toLong())
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

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
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

    private fun sendMessage(
        msg: String,
        type: String,
        fileName: String = "",
        duration: String = ""
    ) {
        val id = getMessages(friendId!!).push().key
        checkNotNull(id) { "Cannot by null" }
        Log.d("Duration", "Duration:$duration")
        getUser.get().addOnSuccessListener {
            val imageUrl = it.get("imageUrl") as String
            val senderName = it.get("name") as String
            if (type == "DOC") {
                msgMap = Message(
                    msg,
                    mCurrentUid,
                    id,
                    imageUrl,
                    senderName,
                    type,
                    fileName = fileName
                )
            } else if (type == "AUDIO") {
                msgMap = Message(
                    msg,
                    mCurrentUid,
                    id,
                    imageUrl,
                    senderName,
                    type,
                    duration = duration
                )
            } else {
                msgMap = Message(msg, mCurrentUid, id, imageUrl, senderName, type)
            }
            getMessages(friendId!!).child(id).setValue(msgMap).addOnSuccessListener {
                Log.d("CHATS", "Completed")
            }.addOnFailureListener {
                Log.d("CHATS", it.localizedMessage)
            }
            updateLastMessage(msgMap, type)
        }

    }

    private fun updateLastMessage(message: Message, type: String) {
        val inboxMap = Inbox(message.msg,friendId!!,mCurrentUid,name!!,name!!.toUpperCase(),image!!,count = 0,type = type)
        getInbox(mCurrentUid, friendId!!).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId!!, mCurrentUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        upper_name=currentUser.name.toUpperCase()
                        image = currentUser.thumbImage
                        count = 1
                    }
                    value?.let {
                        if (it.from == message.senderId) {
                            inboxMap.count = value.count + 1
                        }
                    }
                    getInbox(friendId!!, mCurrentUid).setValue(inboxMap)
                    if (type != "TEXT") {
                        if (::progressDialog.isInitialized)
                            progressDialog.dismiss()
                    }
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
                        mCurrentUid,
                        name!!,
                        name!!.toUpperCase(),
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

    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
            setMessage(message)
        }
    }

    private fun getMessages(friendId: String) =
        db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")

    companion object {
        fun createChatActivity(
            context: Context,
            id: String,
            name: String,
            image: String
        ): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(UID, id)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            return intent
        }
    }
}