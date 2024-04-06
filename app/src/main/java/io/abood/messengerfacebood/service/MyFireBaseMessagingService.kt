package io.abood.messengerfacebood.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.abood.messengerfacebood.ChatActivity
import io.abood.messengerfacebood.MainActivity
import io.abood.messengerfacebood.R
import kotlin.random.Random
const val CHANNEL_ID="channelId"
const val CHANNEL_NAME="myChannel"
class MyFireBaseMessagingService : FirebaseMessagingService(){
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

       // val senderId=message.senderId

        val intent=Intent(this, ChatActivity::class.java).apply {
           // putExtra("senderId",senderId)
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId= Random.nextInt()
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationChannel(notificationManager)
        }
//      when we click on the notification we make sure all activities will destroy
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
       // val pendingIntent= PendingIntent.getActivity(this,0,intent, FLAG_UPDATE_CURRENT)
        val notification=NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setAutoCancel(true)
          //  .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.text_me)
            .build()

        notificationManager.notify(notificationId,notification)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannel(notificationManager: NotificationManager){
        val channel= NotificationChannel(CHANNEL_ID, CHANNEL_NAME,IMPORTANCE_HIGH).apply {
            enableLights(true)
            description="my channel description"
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        Log.d("token:",token)
    }
}
