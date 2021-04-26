package com.project.proabt.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.project.proabt.R
import com.project.proabt.databinding.ActivityInitialReviewProfileBinding
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class InitialReviewProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityInitialReviewProfileBinding
    lateinit var downloadUrl:String
    lateinit var resulturi: Uri
    private val sentphoto by lazy {
        Uri.parse(intent.getStringExtra("SENTPHOTO"))
    }
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInitialReviewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        startCrop(sentphoto)
        binding.btnSend.setOnClickListener {
            Log.d("ClickedFl", "Clicked")
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("ImageURI",resulturi.toString())
            startActivity(intent)
        }
    }
    private fun startCrop(imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setInitialCropWindowPaddingRatio(0F)
            .setAutoZoomEnabled(true)
            .start(this)
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
}