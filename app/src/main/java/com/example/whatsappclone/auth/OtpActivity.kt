package com.example.whatsappclone.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.R.*
import com.example.whatsappclone.databinding.ActivityOtpBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var phoneNumber: String? = null
    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var progressDialog: ProgressDialog
    private var mCounterDown: CountDownTimer? = null
    private var timeLeft: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        startVerify()

    }

    private fun startVerify() {
        startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
        progressDialog = createProgressDialog("Sending a verification code", false)
        progressDialog.show()
    }


    private fun setSpannableString() {
        val span = SpannableString(getString(string.waiting_text, phoneNumber))
        val clickSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ds.linkColor // you can use custom color
                ds.isUnderlineText = false
            }

            override fun onClick(textView: View) {
                showLoginActivity()
            }
        }

        span.setSpan(clickSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.waitingTv.movementMethod = LinkMovementMethod.getInstance()
        binding.waitingTv.text = span
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeft", timeLeft)
        outState.putString(PHONE_NUMBER, phoneNumber)
    }


    private fun showTimer(milliesInFuture: Long) {
        binding.resendBtn.isEnabled = false
        mCounterDown = object : CountDownTimer(milliesInFuture, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                binding.counterTv.isVisible = true
                binding.counterTv.text = "Seconds remaining: " + millisUntilFinished / 1000
            }

            override fun onFinish() {
                binding.resendBtn.isEnabled = true
                binding.counterTv.isVisible = false
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }

    private fun initView() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        binding.verifyTv.text = getString(string.verify_number, phoneNumber)
        setSpannableString()
        binding.verificationBtn.setOnClickListener(this)
        binding.resendBtn.setOnClickListener(this)
        auth = Firebase.auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                val smsMessageSent = credential.smsCode
                if (!smsMessageSent.isNullOrBlank())
                    binding.sentcodeEt.setText(smsMessageSent)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Invalid Phone Number.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.", Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    notifyUserAndRetry("Your Phone Number might be wrong or connection error.Retry again!")
                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                progressDialog.dismiss()
                binding.counterTv.isVisible = false
                mVerificationId = verificationId
                mResendToken = token

            }
        }
    }

    override fun onClick(v: View) {
        when (v) {
            binding.verificationBtn -> {
                val code = binding.sentcodeEt.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrEmpty()) {
                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()
                    val credential =
                        PhoneAuthProvider.getCredential(mVerificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
            }

            binding.resendBtn -> {
                if (mResendToken != null) {
                    resendVerificationCode(phoneNumber.toString(), mResendToken)
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a verification code", false)
                    progressDialog.show()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Sorry, You Can't request new code now, Please wait",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (::progressDialog.isInitialized) {
                        progressDialog.dismiss()
                    }

                    if (task.result?.additionalUserInfo?.isNewUser == true) {
                        showSignUpActivity()
                    } else {
                        showHomeActivity()
                    }
                } else {
                    if (::progressDialog.isInitialized) {
                        progressDialog.dismiss()
                    }
                    notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!")
                }
            }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }

    private fun showLoginActivity() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun showHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        mResendToken: PhoneAuthProvider.ForceResendingToken?
    ) {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
        if (mResendToken != null) {
            options.setForceResendingToken(mResendToken)
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    override fun onBackPressed() {

    }
}

fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
    return ProgressDialog(this).apply {
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(false)
        setMessage(message)
    }
}

