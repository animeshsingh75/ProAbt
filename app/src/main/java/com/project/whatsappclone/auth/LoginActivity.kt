package com.project.whatsappclone.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.credentials.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.whatsappclone.databinding.ActivityLoginBinding


const val CREDENTIAL_PICKER_REQUEST = 1

class LoginActivity : AppCompatActivity() {
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestHint()
        binding.phoneNumberEt.addTextChangedListener {
            binding.nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }
        binding.nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setHintPickerConfig(
                CredentialPickerConfig.Builder()
                    .setShowCancelButton(true)
                    .build()
            )
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val intent = Credentials.getClient(this).getHintPickerIntent(hintRequest)
        startIntentSenderForResult(intent.intentSender, CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK) {
            val credentials: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            binding.phoneNumberEt.setText(credentials?.id?.substring(3))
        } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            Log.i("TAG", "Google Play Services Error")
        }
    }

    private fun checkNumber() {
        countryCode = binding.ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + binding.phoneNumberEt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(
                "We will be verifying the phone number:$phoneNumber\n" +
                        "Is this OK, or would you like to edit the number?"
            )
            setPositiveButton("Ok") { _, _ ->
                showOtpActivity()
            }
            setNegativeButton("Edit") { dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this, OtpActivity::class.java).putExtra(PHONE_NUMBER, phoneNumber))
        finish()
    }
}