package com.project.proabt.setting

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.proabt.R
import com.project.proabt.databinding.ActivityEditProfBinding
import com.project.proabt.models.Inbox
import com.project.proabt.models.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*


class EditProfActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivityEditProfBinding
    lateinit var currentUser: User
    lateinit var downloadUrl: String
    lateinit var thumbnailUrl: String
    var changeInitial=true
    private lateinit var progressDialog: ProgressDialog
    lateinit var inboxMap:Inbox
    lateinit var skillsMap: List<String>
    var isInitial = true
    var skills = arrayOf("C++", "C++", "C++")
    var size = 0
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
        binding = ActivityEditProfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
                Picasso.get()
                    .load(currentUser.thumbImage)
                    .placeholder(R.drawable.defaultavatar)
                    .error(R.drawable.defaultavatar)
                    .into(binding.userImgView)
                binding.nameEt.text = Editable.Factory.getInstance().newEditable(currentUser.name)
                Log.d("Skills", currentUser.skills.size.toString())
                size=currentUser.skills.size
                if (size >= 1) {
                    binding.skillLayout2.isVisible = true
                    skills[0]=currentUser.skills[0]
                    binding.skillTv1.text = skills[0]

                }
                if (size >= 2) {
                    skills[1]=currentUser.skills[1]
                    binding.skillLayout2.isVisible = true
                    binding.skillTv2.text = skills[1]

                }
                if (size == 3) {
                    skills[2]=currentUser.skills[2]
                    binding.skillLayout3.isVisible = true
                    binding.skillTv3.text = skills[2]
                }
            }
        binding.userImgView.setOnClickListener {
            Log.d("Clicked", "Clicked")
            showPopup(it)
        }
        binding.closeBtn.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        binding.confirmBtn.setOnClickListener{
            progressDialog = createProgressDialog("Updating Data. Please wait", false)
            progressDialog.show()
            if (size == 1) {
                skillsMap = listOf(skills[0])
            } else if (size == 2) {
                skillsMap = listOf(skills[0], skills[1])
            } else {
                skillsMap = listOf(skills[0], skills[1], skills[2])

            }
            val query=FirebaseDatabase.getInstance().reference.child("chats").orderByChild("name")
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(changeInitial){
                        for(datasnapshot in snapshot.children){
                            if(datasnapshot.child(mCurrentUid).value!=null && changeInitial){
                                changeInitial=false
                                val inbox=datasnapshot.child(mCurrentUid).getValue(Inbox::class.java)
                                Log.d("Inbox",inbox!!.name)
                                inbox.apply {
                                    name = binding.nameEt.text.toString()
                                    upper_name=binding.nameEt.text.toString().toUpperCase()
                                }
                                Log.d("Inbox",inbox.name)
                                FirebaseDatabase.getInstance().getReference("chats").child(inbox.to).child(inbox.from).setValue(inbox).addOnSuccessListener {
                                    finish()
                                    startActivity(Intent(this@EditProfActivity,SettingActivity::class.java))
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            currentUser = User(
                binding.nameEt.text.toString(),
                binding.nameEt.text.toString().toUpperCase(),
                currentUser.imageUrl,
                currentUser.thumbImage,
                currentUser.uid,
                currentUser.deviceToken,
                currentUser.rating,
                skillsMap
            )
            database.collection("users").document(mCurrentUid).set(currentUser)
                .addOnSuccessListener {

                    startActivity(Intent(this, SettingActivity::class.java))

                }
        }
        Log.d("Skills", size.toString())
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
        btn_camera_x_button.setOnClickListener {
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
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(mCurrentUid).get()
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
                                        database.collection("users").document(mCurrentUid)
                                            .set(currentUser)
                                            .addOnSuccessListener {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        SettingActivity::class.java
                                                    )
                                                )

                                            }.addOnFailureListener {
                                            }
                                    }
                            }
                    }
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

    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
            setMessage(message)
        }
    }
}