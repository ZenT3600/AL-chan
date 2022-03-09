package it.matteoleggio.alchan.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import it.matteoleggio.alchan.notifications.PushNotificationsService

class BroadcastReceiverNotifs : BroadcastReceiver() {
    override fun onReceive(contxt: Context?, intent: Intent?) {
        val intent = Intent(contxt, PushNotificationsService::class.java)
        println("checking notifs")
        PushNotificationsService().enqueueWork(contxt!!, intent)
    }
}