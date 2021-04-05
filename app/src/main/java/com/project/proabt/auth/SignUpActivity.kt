package com.project.proabt.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.proabt.MainActivity
import com.project.proabt.R
import com.project.proabt.databinding.ActivitySignUpBinding
import com.project.proabt.models.User
import com.project.proabt.service.MyFirebaseMessaging
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*


class SignUpActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivitySignUpBinding
    lateinit var downloadUrl: String
    lateinit var thumbnailUrl: String
    lateinit var skillsMap: List<String>
    var skills = arrayOf("C++", "C++", "C++")
    var size = 0
    var isInitial = true
    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.userImgView.setOnClickListener {
            checkPermissionForImage()
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.skillsList,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.skillsSpinner.adapter = adapter
        }
        binding.closeBtn1.setOnClickListener {
            if (size == 1) {
                binding.skillsContainer.isVisible = false
            } else if (size == 2) {
                skills[0] = skills[1]
                binding.skillTv1.text = binding.skillTv2.text
                binding.skillLayout2.isVisible = false
            } else {
                skills[0] = skills[1]
                skills[1] = skills[2]
                binding.skillTv1.text = binding.skillTv2.text
                binding.skillTv2.text = binding.skillTv3.text
                binding.skillLayout3.isVisible = false
            }
            size--
            Log.d("Skills", "Index0 ${skills[0]}")
            Log.d("Skills", "Index1 ${skills[1]}")
            Log.d("Skills", "Index2 ${skills[2]}")
            Log.d("Skills", "$size")
        }
        binding.closeBtn2.setOnClickListener {
            if (size == 2) {
                binding.skillLayout2.isVisible = false
            } else {
                skills[1] = skills[2]
                binding.skillTv2.text = binding.skillTv3.text
                binding.skillLayout3.isVisible = false
            }
            size--
            Log.d("Skills", "Index0 ${skills[0]}")
            Log.d("Skills", "Index1 ${skills[1]}")
            Log.d("Skills", "Index2 ${skills[2]}")
            Log.d("Skills", "$size")
        }
        binding.closeBtn3.setOnClickListener {
            binding.skillLayout3.isVisible = false
            size--
            Log.d("Skills", "Index0 ${skills[0]}")
            Log.d("Skills", "Index1 ${skills[1]}")
            Log.d("Skills", "Index2 ${skills[2]}")
            Log.d("Skills", "$size")
        }
        binding.skillsSpinner.onItemSelectedListener = this
        binding.nextBtn.setOnClickListener {
            binding.nextBtn.isEnabled = false
            val name = binding.nameEt.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot by empty", Toast.LENGTH_LONG).show()
            } else if (size == 0) {
                Toast.makeText(this, "Atleast add one skill", Toast.LENGTH_LONG).show()
            } else if (!::downloadUrl.isInitialized) {
                Toast.makeText(this, "Image cannot by empty", Toast.LENGTH_LONG).show()
            } else {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.d("FCM", "Fetching FCM Registration token failed", task.exception)
                        return@OnCompleteListener
                    }
                    token = task.result
                    MyFirebaseMessaging.sendRegistrationToken(token)
                })
                val ref =
                    storage.reference.child("profile_pics/" + auth.uid.toString() + "_120x120").downloadUrl.addOnSuccessListener {
                        thumbnailUrl = it.toString()
                        Log.d("URL", "thumbnailUrl: $thumbnailUrl")
                        if (size == 1) {
                            skillsMap = listOf(skills[0])
                        } else if (size == 2) {
                            skillsMap = listOf(skills[0], skills[1])
                        } else {
                            skillsMap = listOf(skills[0], skills[1], skills[2])

                        }
                        val user = User(
                            name,
                            downloadUrl,
                            thumbnailUrl,
                            auth.uid!!,
                            token,
                            0F,
                            skills = skillsMap
                        )
                        database.collection("users").document(auth.uid!!).set(user)
                            .addOnSuccessListener {

                                startActivity(Intent(this, MainActivity::class.java))

                            }.addOnFailureListener {
                                binding.nextBtn.isEnabled = true
                            }
                    }
            }
        }
    }

    override fun onBackPressed() {
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

    private fun uploadImage(it: Uri) {
        binding.nextBtn.isEnabled = false
        val ref = storage.reference.child("profile_pics/" + auth.uid.toString())
        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            binding.nextBtn.isEnabled = true
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                Log.d("URL", "downloadUrl: $downloadUrl")
            }
        }.addOnFailureListener {

        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent!!.getItemAtPosition(position).toString()
        if (isInitial) {
            isInitial = false
        } else if (size == 3) {
            Toast.makeText(this, "You can select at max 3 skills", Toast.LENGTH_SHORT).show()
        } else if (item == "--Not selected--") {

        } else {
            skills[size] = item
            size++
            if (size == 1) {
                binding.skillsContainer.isVisible = true
                binding.skillLayout1.isVisible = true
                binding.skillTv1.text = item
            } else if (size == 2) {
                binding.skillLayout2.isVisible = true
                binding.skillTv2.text = item
            } else {
                binding.skillLayout3.isVisible = true
                binding.skillTv3.text = item
            }
            Log.d("Skills", "Index0 ${skills[0]}")
            Log.d("Skills", "Index1 ${skills[1]}")
            Log.d("Skills", "Index2 ${skills[2]}")
            Log.d("Skills", "$size")
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}