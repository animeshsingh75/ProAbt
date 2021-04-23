package com.project.proabt.attachment_types

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.project.proabt.IMAGE
import com.project.proabt.NAME
import com.project.proabt.R
import com.project.proabt.databinding.ActivityViewVideoBinding
import com.squareup.picasso.Picasso

class ViewVideoActivity : AppCompatActivity() {
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }
    private val msg by lazy {
        Uri.parse(intent.getStringExtra("MSG"))
    }
    lateinit var binding: ActivityViewVideoBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = createProgressDialog("Buffering.... Please wait", false)
        progressDialog.show()
        binding.nameTv.text=name
        if(name=="YOU"){
            binding.userImgView.isVisible=false
        }else{
            Picasso.get()
                .load(image)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(binding.userImgView)
        }
        Log.d("VideoView",msg.toString())
        val mediaController= MediaController(this)
        mediaController.setAnchorView(binding.sentVideo)
        binding.sentVideo.setMediaController(mediaController)
        binding.sentVideo.setVideoURI(msg)
        binding.sentVideo.requestFocus()
        binding.sentVideo.setOnPreparedListener {
            progressDialog.dismiss()
            binding.sentVideo.start()
        }
        binding.btnBack.setOnClickListener {
            finish()
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