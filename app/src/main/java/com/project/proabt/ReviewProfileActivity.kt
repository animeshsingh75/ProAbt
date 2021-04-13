package com.project.proabt

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.proabt.databinding.ActivityReviewProfileBinding
import com.project.proabt.models.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.IOException

class ReviewProfileActivity : AppCompatActivity() {
    private val sentphoto by lazy {
        Uri.parse(intent.getStringExtra("SENTPHOTO"))
    }
    val picturePath by lazy {
        intent.getStringExtra("PicturePath")
    }
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var binding: ActivityReviewProfileBinding
    private lateinit var progressDialog: ProgressDialog
    var angle: Float? = 0F
    lateinit var downloadUrl: String
    lateinit var thumbnailUrl:String
    lateinit var currentUser: User
    lateinit var resulturi: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("PhotoUrifromReviw", sentphoto.toString())
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
        startCrop(sentphoto)
        binding.btnSend.setOnClickListener {
            Log.d("ClickedFl", "Clicked")
            uploadImage(resulturi)
            progressDialog = createProgressDialog("Sending a photo. Please wait", false)
            progressDialog.show()
        }
        angle = getOrientation().toFloat()
        Log.d("Rotation", angle.toString())
    }

    private fun uploadImage(it: Uri) {
        val ref = storage.reference.child("profile_pics/" + mCurrentUid)
        ref.delete()
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
                database.collection("users").document(mCurrentUid).get()
                    .addOnSuccessListener {
                        val ref =
                            storage.reference.child("profile_pics/" + mCurrentUid + "_120x120").downloadUrl.addOnSuccessListener {
                                thumbnailUrl = it.toString()
                                FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
                                    .addOnSuccessListener {
                                        currentUser = it.toObject(User::class.java)!!
                                        currentUser = User(
                                            currentUser.name,
                                            currentUser.upper_name,
                                            downloadUrl,
                                            thumbnailUrl,
                                            currentUser.uid,
                                            currentUser.deviceToken,
                                            currentUser.rating,
                                            currentUser.skills
                                        )
                                        database.collection("users").document(mCurrentUid).set(currentUser)
                                            .addOnSuccessListener {

                                                startActivity(Intent(this, MainActivity::class.java))

                                            }.addOnFailureListener {
                                            }
                                    }
                            }
                    }

            }
        }.addOnFailureListener {

        }
    }

    private fun startCrop(imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setInitialCropWindowPaddingRatio(0F)
            .setAutoZoomEnabled(true)
            .start(this)
    }

    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
            setMessage(message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                resulturi = result.uri
                Picasso.get()
                    .load(resulturi)
                    .placeholder(R.drawable.defaultavatar)
                    .error(R.drawable.defaultavatar)
                    .into(binding.sentImage)
            }
        }
    }
    private val ROTATION_DEGREES = 90
    @Throws(IOException::class)
    fun getOrientation(): Int {
        val exif = ExifInterface(picturePath!!)
        var orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> orientation = ROTATION_DEGREES
            ExifInterface.ORIENTATION_ROTATE_180 -> orientation = ROTATION_DEGREES * 2
            ExifInterface.ORIENTATION_ROTATE_270 -> orientation = ROTATION_DEGREES * 3
            else ->
                orientation = 0
        }
        return orientation
    }
}
