package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this,LoginActivity::class.java))
//        if(auth.currentUser==null){
//            startActivity(Intent(this,LoginActivity::class.java))
//        }
//        else{
//            startActivity(Intent(this,MainActivity::class.java))
//        }
    }
}