package com.project.proabt.attachment_types

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.common.util.concurrent.ListenableFuture
import com.project.proabt.*
import com.project.proabt.R
import com.project.proabt.databinding.ActivityVideoCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class VideoCameraActivity : AppCompatActivity() {

    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }

    companion object {
        @JvmStatic
        val CAMERA_PERM_CODE = 422
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = appContext.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
        }
    }
    lateinit var file:File
    var isVideo = false
    var second = 0
    private var isInitial = true
    var lastSecond = 0
    var minute = 0
    var blink = false
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    lateinit var countDownTimer: CountDownTimer
    private val executor = Executors.newSingleThreadExecutor()
    lateinit var binding: ActivityVideoCameraBinding
    lateinit var videoCapture: VideoCapture
    private var camera: Camera? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askCameraPermission()
        binding.btnTakePhoto.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.timeLayout.isVisible=true
                showTimer()
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (isVideo) {
                    val UriVideo= Uri.fromFile(file)
                    Log.d("VideoUri",UriVideo.toString())
                    videoCapture.stopRecording()
                    countDownTimer.cancel()
                    val intent = Intent(this, ReviewVideoActivity::class.java)
                    intent.putExtra(UID, friendId)
                    intent.putExtra(NAME, name)
                    intent.putExtra(IMAGE, image)
                    intent.putExtra("SENTVIDEO",UriVideo.toString())
                    startActivity(intent)
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun askCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ),
            CAMERA_PERM_CODE
        )
    }

    @SuppressLint("RestrictedApi")
    private fun startVideoCamera() {
        outputDirectory = getOutputDirectory(this)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        videoCapture = VideoCapture.Builder().apply {
            setTargetRotation(binding.previewView.display.rotation)
            setCameraSelector(cameraSelector)
        }.build()

        val preview: Preview = Preview.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            setTargetRotation((binding.previewView.display.rotation))
        }.build()
        preview.setSurfaceProvider((binding.previewView.surfaceProvider))

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                videoCapture
            )

        }, ContextCompat.getMainExecutor(this))
    }


    fun showTimer() {
        second = 0
        minute = 0
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 100) {
            @SuppressLint("RestrictedApi")
            override fun onTick(millisUntilFinished: Long) {
                if (Long.MAX_VALUE - millisUntilFinished > 300) {
                    if (isInitial) {
                        isVideo = true
                        binding.recRedCircle.isVisible = true
                        binding.timeLayout.isVisible = true
                        isInitial = false
                        startRecording()
                    }
                    second = ((Long.MAX_VALUE - millisUntilFinished) / 1000).toInt()
                    Log.d("Seconds", second.toString())
                    binding.timeTv.text = recorderTime()
                    if (second == 59) {
                        second = 0
                        Log.d("Seconds", "Inside")
                        Toast.makeText(
                            this@VideoCameraActivity,
                            "Cannot record for more than 59 secs",
                            Toast.LENGTH_LONG
                        ).show()
                        countDownTimer.cancel()
                        videoCapture.stopRecording()
                        val intent = Intent(this@VideoCameraActivity, ChatActivity::class.java)
                        intent.putExtra(UID, friendId)
                        intent.putExtra(NAME, name)
                        intent.putExtra(IMAGE, image)
                        startActivity(intent)
                    }
                }

            }

            override fun onFinish() {}
        }
        countDownTimer.start()
    }

    fun recorderTime(): String? {
        if (blink && second != lastSecond) {
            binding.recLogo.visibility = View.INVISIBLE
            blink = !blink
        } else if (!blink && second != lastSecond) {
            binding.recLogo.visibility = View.VISIBLE
            blink = !blink
        }
        lastSecond = second
        return java.lang.String.format("%01d:%02d", minute, second)
    }

    @SuppressLint("MissingPermission", "RestrictedApi")
    private fun startRecording() {
        file = File(
            getOutputDirectory(this),
            SimpleDateFormat(
                "yyyyMMddHHmmssSSS", Locale.US
            ).format(System.currentTimeMillis()) + ".3gp"
        )
        val outputFileOptions =
            VideoCapture.OutputFileOptions.Builder(file).build()
        videoCapture.startRecording(
            outputFileOptions,
            executor,
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    val videoUri=Uri.fromFile(file)
                    Log.d("PhotoUri", "PhotoUri:$videoUri")
                    Log.d("PhotoUri", "PhotoUri:${file.absolutePath}")
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this@VideoCameraActivity,
                            "Image Saving Failed",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("CAMERA1", "Image Saving Failed", cause)
                        Log.d("CAMERA1", message)
                    }
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVideoCamera()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Error")
                    .setMessage("Camera Permission not provided")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}