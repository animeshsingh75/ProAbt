package com.project.proabt

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.project.proabt.databinding.ActivityViewImageBinding
import com.project.proabt.models.Message
import com.squareup.picasso.Picasso

class ViewImageActivity : AppCompatActivity() {
    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }
    private val msg by lazy {
        Uri.parse(intent.getStringExtra("MSG"))
    }
    lateinit var binding:ActivityViewImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.nameTv.text=name
        if(name=="YOU"){
            binding.userImgView.isVisible=false
        }
        Picasso.get()
            .load(image)
            .placeholder(R.drawable.defaultavatar)
            .error(R.drawable.defaultavatar)
            .into(binding.userImgView)
        Picasso.get()
            .load(msg)
            .placeholder(R.drawable.defaultavatar)
            .error(R.drawable.defaultavatar)
            .into(binding.sentImage)
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}