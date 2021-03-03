package com.project.proabt.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.project.proabt.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("FCM", "RefreshedToken:$token")
        sendRegistrationToken(token)
    }

    companion object {
        fun sendRegistrationToken(token: String) {
            FirebaseFirestore.getInstance()
                .document("users/${FirebaseAuth.getInstance().currentUser?.uid}").update(
                    mapOf("deviceToken" to token)
                )
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.wtf("TAG", "From: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            sendNotification(it, remoteMessage.data)
            Log.wtf("Title", "Message Notification Body: ${it.title}")
            Log.d("TAG", "Message Notification Body: ${it.body}")
        }
    }

    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: MutableMap<String, String>
    ) {
//        val uid = data["UID"]
//        val name = data["NAME"]
//        val image = data["IMAGE"]
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Whatsapp Clone",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

