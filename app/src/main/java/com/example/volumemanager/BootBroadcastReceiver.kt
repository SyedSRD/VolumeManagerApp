package com.example.volumemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Launch the specified service when this message is received
        val startServiceIntent = Intent(context, MyAlarmReceiver::class.java)
        context.startService(startServiceIntent)
    }
}