package com.example.my_car.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.my_car.MainActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            // Direct Activity Launch
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Foreground Service Full-Screen Launch Failsafe
            try {
                val serviceIntent = Intent(context, BootService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
