package com.project.proabt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.proabt.auth.LoginActivity
import com.project.proabt.auth.SignUpActivity
import com.project.proabt.models.User

class SplashActivity : AppCompatActivity() {
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    lateinit var currentUser:User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val database by lazy{
            FirebaseFirestore.getInstance().collection("users").document(auth.uid!!)
        }
        val handler = Handler().postDelayed({
            if (auth.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else if(auth.currentUser!=null){
                database.get().addOnSuccessListener {
                    if(it.exists()){
                        Log.d("Exists","Exists")
                        startActivity(Intent(this, MainActivity::class.java))
                    }else{
                        Log.d("Exists","Does not Exist")
                        val intent=Intent(this, SignUpActivity::class.java)
                        intent.putExtra("ImageURI","")
                        startActivity(intent)
                    }
                }
            }
        }, 2500)

    }
}