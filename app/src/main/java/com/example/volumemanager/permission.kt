package com.example.volumemanager

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import com.google.android.material.floatingactionbutton.FloatingActionButton

class permission : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val PermBtn1 =findViewById<Button>(R.id.perm_btn1)
        //saveAndLock.isChecked=true
        val PermBtn2 =findViewById<Button>(R.id.perm_btn2)
        val PermBtn3 =findViewById<Button>(R.id.perm_btn3)

        if (notificationManager.isNotificationPolicyAccessGranted){
            //toast("Notification policy access granted.")
            PermBtn1.setTextColor(getColor(R.color.silver))
            PermBtn1.isEnabled=false
        }
        if (Settings.canDrawOverlays(applicationContext)) {
            PermBtn2.setTextColor(getColor(R.color.silver))
            PermBtn2.isEnabled=false
        }
        if (Settings.System.canWrite(applicationContext)){
            PermBtn3.setTextColor(getColor(R.color.silver))
            PermBtn3.isEnabled=false
        }

        PermBtn1.setOnClickListener{
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            //intent.data = Uri.parse("package:" + packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }
        PermBtn2.setOnClickListener{
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.setData(Uri.parse("package:" + packageName))
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(myIntent)
        }
        PermBtn3.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
            }
        }

    }

}