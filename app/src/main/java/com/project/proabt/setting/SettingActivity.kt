package com.project.proabt.setting

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.proabt.MainActivity
import com.project.proabt.R
import com.project.proabt.auth.LoginActivity
import com.project.proabt.databinding.ActivitySettingBinding
import com.project.proabt.models.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import hotchemi.android.rate.AppRate


class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding
    lateinit var currentUser: User
    lateinit var downloadUrl: String
    lateinit var thumbnailUrl: String
    private lateinit var progressDialog: ProgressDialog
    val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
                Picasso.get()
                    .load(currentUser.thumbImage)
                    .placeholder(R.drawable.defaultavatar)
                    .error(R.drawable.defaultavatar)
                    .into(binding.userImgView)
            }
        binding.userImgView.setOnClickListener {
            Log.d("Clicked", "Clicked")
            showPopup(it)
        }
        binding.account.setOnClickListener {
            startActivity(Intent(this,EditProfActivity::class.java))
        }
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.viewRating.setOnClickListener {
            startActivity(Intent(this, ShowRatingActivity::class.java))
        }
        binding.rateApp.setOnClickListener {
            AppRate.with(this).showRateDialog(this)
        }
    }

    private fun showPopup(anchorView: View) {
        val layout = layoutInflater.inflate(R.layout.photo_selector, null)
        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val popupWindow = PopupWindow(layout, ViewPager.LayoutParams.MATCH_PARENT, 900, true)
        popupWindow.isOutsideTouchable = false
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 70, 140)
        popupWindow.isFocusable = true
        val closeBtn = layout.findViewById<ImageView>(R.id.closeBtn)
        val btn_camera_x_button = layout.findViewById<ShapeableImageView>(R.id.btn_camera_x_button)
        val btn_gallery_button = layout.findViewById<ShapeableImageView>(R.id.btn_gallery_button)
        closeBtn.setOnClickListener {
            popupWindow.dismiss()
        }
        btn_gallery_button.setOnClickListener {
            checkPermissionForImage()
        }
        btn_camera_x_button.setOnClickListener{
            val intent = Intent(this, ProfileCameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissionForImage() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED && (checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED)
        ) {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(
                permission,
                1001
            )
            requestPermissions(
                permissionWrite,
                1002
            )
        } else {
            pickImageFromGallery()
        }
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
            startCrop(imageUri)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                binding.userImgView.setImageURI(result.uri)
                progressDialog = createProgressDialog("Sending a photo. Please wait", false)
                progressDialog.show()
                uploadImage(result.uri)
            }
        }
    }

    private fun startCrop(imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    private fun uploadImage(uri: Uri) {
        val ref = storage.reference.child("profile_pics/" + mCurrentUid)
        ref.delete().addOnSuccessListener {
            val uploadTask = ref.putFile(uri)
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
                    Log.d("URL", "downloadUrl: $downloadUrl")
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

    }
    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
            setMessage(message)
        }
    }
}


