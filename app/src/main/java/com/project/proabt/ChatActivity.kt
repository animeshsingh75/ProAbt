package com.project.proabt

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.project.proabt.attachment_types.ReviewVideoActivity
import com.project.proabt.attachment_types.VideoCameraActivity
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
import java.io.File
import java.util.*


const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

var mediaPlayer:MediaPlayer?=null
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
    var mediaRecorder: MediaRecorder? = null
    private lateinit var progressDialog: ProgressDialog
    lateinit var recordFile: String
    lateinit var downloadUrl: String
    lateinit var countDownTimer: CountDownTimer
    lateinit var filePath: String
    var second = -1
    var minute = 0
    var blink = false
    private val messages = mutableListOf<ChatEvent>()
    lateinit var chatAdapter: ChatAdapter
    lateinit var currentUser: User
    lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.rateUser -> {
                val intent = Intent(this, RateUserActivity::class.java)
                intent.putExtra(NAME, name)
                intent.putExtra(UID, friendId)
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
        setSupportActionBar(binding.toolbar)
        checkInitialMessage()
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            if(mediaPlayer!=null) {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            startActivity(Intent(this,MainActivity::class.java))
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
        Log.d(
            "MicBtn",
            "${binding.msgEdTv.text.isNullOrEmpty() || binding.msgEdTv.text.isNullOrBlank()}"
        )
        binding.msgEdTv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("Text", s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    binding.micBtn.isVisible = true
                } else {
                    binding.micBtn.isVisible = false
                    binding.sendBtn.isVisible = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.sendAudioBtn.setOnClickListener {
            Log.d("Clicked", "Clicked")
            binding.inputBox.isVisible = true
            binding.recordAudio.isVisible = false
            countDownTimer.cancel()
            sendAudio()
        }
        binding.micBtn.setOnClickListener {
            Log.d("MicBtn", "Clicked")
            binding.layoutActionsContainer.isVisible = false
            attachmentclick = false
            askAudioPermission()
            binding.inputBox.isVisible = false
            binding.recordAudio.isVisible = true
        }
        binding.deleteAudio.setOnClickListener {
            binding.inputBox.isVisible = true
            binding.recordAudio.isVisible = false
            countDownTimer.cancel()
            stopRecording()
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
        binding.btnVideoCameraButton.setOnClickListener {
            val intent = Intent(this, VideoCameraActivity::class.java)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            startActivity(intent)
        }
        binding.btnVideoCamera.setOnClickListener {
            val intent = Intent(this, VideoCameraActivity::class.java)
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
        binding.btnVideo.setOnClickListener{
            pickVideo()
        }
        binding.btnVideoButton.setOnClickListener {
            pickVideo()
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
        Log.d("Detached","Detached")
        if(mediaPlayer!=null){
            Log.d("Detached","Detached")
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer=null
        }
        startActivity(Intent(this,MainActivity::class.java))
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }
    private fun pickVideo(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(
            intent,
            1003
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

    private fun startRecording() {
        Log.d("Recording", "Started Recording")
        val recordPath = this.getExternalFilesDir("/")!!.absolutePath
        recordFile = System.currentTimeMillis().toString() + ".mpeg"
        filePath = "$recordPath/$recordFile"
        mediaRecorder = MediaRecorder()
        showTimer()
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile("$recordPath/$recordFile")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        val file = File(filePath)
        file.delete()
        mediaRecorder = null
    }

    private fun sendAudio() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        val savedUri = Uri.fromFile(File(filePath))
        progressDialog = createProgressDialog("Sending a Audio. Please wait", false)
        progressDialog.show()
        uploadAudio(savedUri, binding.timeTv.text.toString())
    }

    private fun askAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            1002
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1002) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Error")
                    .setMessage("Audio Permission not provided")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        if (resultCode == Activity.RESULT_OK && requestCode == 1003) {
            val videoUri = data?.data
            Log.d("SENTVIDEO", "URI:${videoUri.toString()}")
            val videoPath = PathUtils.getPath(videoUri!!, this)
            Log.d("SENTVIDEO", videoPath!!)
            val intent = Intent(this, ReviewVideoActivity::class.java)
            intent.putExtra("VideoPath", videoPath)
            intent.putExtra(UID, friendId)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            intent.putExtra("SENTVIDEO", videoUri.toString())
            startActivity(intent)
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

    fun showTimer() {
        second = 0
        minute = 0
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                second++
                binding.timeTv.text = recorderTime()
                if (minute == 10) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Cannot record for more than 9:59 mins",
                        Toast.LENGTH_LONG
                    ).show()
                    countDownTimer.cancel()
                    binding.inputBox.isVisible = true
                    binding.recordAudio.isVisible = false
                }
            }

            override fun onFinish() {}
        }
        countDownTimer.start()
    }

    fun recorderTime(): String? {
        if (second === 60) {
            minute++
            second = 0
        }
        if (blink) {
            binding.recLogo.visibility = View.INVISIBLE
            blink = !blink
        } else {
            binding.recLogo.visibility = View.VISIBLE
            blink = !blink
        }
        return java.lang.String.format("%01d:%02d", minute, second)
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
        val InvertedDate=Long.MAX_VALUE-Date().time
        Log.d("Inverted",(Long.MAX_VALUE-InvertedDate).toString())
        val inboxMap = Inbox(
            message.msg,
            friendId!!,
            mCurrentUid,
            name!!,
            name!!.toUpperCase(),
            image!!,
            count = 0,
            type = type,
            invertedDate = InvertedDate
        )
        getInbox(mCurrentUid, friendId!!).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId!!, mCurrentUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    val InvertedDate=Long.MAX_VALUE-Date().time
                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        upper_name = currentUser.name.toUpperCase()
                        image = currentUser.thumbImage
                        count = 1
                        invertedDate=InvertedDate
                    }
                    value?.let {
                        if (it.from == message.senderId) {
                            inboxMap.count = value.count + 1
                            inboxMap.invertedDate=InvertedDate
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
                        count = 0,
                        invertedDate = 0
                    )
                    getInbox(mCurrentUid, friendId!!).setValue(inboxMap).addOnCompleteListener {
                    }
                } else {
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