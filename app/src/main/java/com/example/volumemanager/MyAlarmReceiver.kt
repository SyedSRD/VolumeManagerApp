package com.example.volumemanager

import android.R
import android.R.color
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.NotificationCompat
import java.util.*


class MyAlarmReceiver : BroadcastReceiver() {
    // Triggered by the Alarm periodically (starts the service to run task)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onReceive(context: Context, intent: Intent?) {
        /*val i = Intent(context, MyTestService::class.java)
        i.putExtra("foo", "bar")
        context.startService(i)
        Toast.makeText(context, "Welcome --------1", Toast.LENGTH_LONG).show()
         */
        var str1:String="";var E_Time:String=""
        val audioManager: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (intent != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, intent.getIntExtra("Media", 0), AudioManager.FLAG_SHOW_UI)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, intent.getIntExtra("Ring", 0), AudioManager.FLAG_SHOW_UI)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, intent.getIntExtra("Alarm", 0), AudioManager.FLAG_SHOW_UI)
            //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)//, intent.getIntExtra("Alarm", 0), AudioManager.FLAG_SHOW_UI)
            if(notificationManager.isNotificationPolicyAccessGranted) {
                if (intent.getBooleanExtra("DND", false)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                        Log.d("MyAlarmReceiver", " DND is turnd on :")
                    }
                    Log.d("MyAlarmReceiver", " DND is turnd on kkk :")
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        Log.d("MyAlarmReceiver", " DND is turnd off :")
                    }
                    Log.d("MyAlarmReceiver", " DND is turnd off kkk :")
                }
            }
            if(intent.getBooleanExtra("Vibrate", false)){
                Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 1)
            }
                //setAirPlaneMode(context,intent.getBooleanExtra("Airplane", false))

            str1=" Media :" + intent.getIntExtra("Media", 0).toString() +
                    " \tRing :" + intent.getIntExtra("Ring", 0).toString() +
                    " \tAlarm :" + intent.getIntExtra("Alarm", 0).toString() +
                    "\t DND : " + intent.getBooleanExtra("DND", false).toString()+
                    "\t Airplane : " + intent.getBooleanExtra("Airplane", false).toString() +
                    "\t Vibrate : " + intent.getBooleanExtra("Vibrate", false).toString()

            //@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            E_Time=intent.getStringExtra("E_Time")
            Log.d("MyAlarmReceiver", str1 )

                    // "\t JustVib : " + intent.getBooleanExtra("JustVib", false).toString()

        }
        Log.d("MyAlarmReceiver", " BroadcastReceiver :" + Date().toString())
        val res: Resources = context.resources

        if(E_Time!="") {
            var notificationManager2: NotificationManager
            var notificationChannel: NotificationChannel
            var builder: Notification.Builder
            val channelId = "i.apps.notifications"
            val description = "Task notification"

            notificationManager2 = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = NotificationChannel(
                        channelId, description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(false)
                notificationManager2.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(context, channelId)
                        .setContentTitle("Schedule task is running")
                        .setContentText("This task will end at " +E_Time)
                        .setSmallIcon(R.drawable.ic_lock_idle_alarm)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ic_lock_idle_alarm))
                //.setContentIntent(pendingIntent)
            } else {
                builder = Notification.Builder(context)
                        .setContentTitle("Schedule task is running")
                        .setContentText("This task will end at " +E_Time)
                        .setSmallIcon(R.drawable.ic_lock_idle_alarm)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ic_lock_idle_alarm))
                        .setColor(context.getColor(R.color.holo_blue_light))
                //.setContentIntent(pendingIntent)
            }
            notificationManager2.notify(1234, builder.build())
        }
    }


    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) !== 0
    }
    fun setAirPlaneMode(context: Context, airplaneMode: Boolean) {
        Log.d("MyAlarmReceiver", "setAirPlaneMode airplaneMode: " + airplaneMode)
        val state = if (airplaneMode) 1 else 0
        if (android.os.Build.VERSION.SDK_INT < 17) {
            try {
                Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, state);
                val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                intent.putExtra("state", state)
                context.sendBroadcast(intent)
            } catch (e: ActivityNotFoundException) {
                Log.d("MyAlarmReceiver", e.message);
                print("Build.VERSION.SDK_INT "+e.message)
            }
        }
        else {
            try {
                Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, state);
                val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                intent.putExtra("state", state)
                context.sendBroadcast(intent)
            } catch (e: ActivityNotFoundException) {
                Log.d("MyAlarmReceiver", e.message);
                print("Build.VERSION.SDK_INT>17 "+e.message)
            }
        }


    }
    companion object {
        const val REQUEST_CODE = 12345
    }
}