package com.example.mindaugas.adventureapp

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionsIntentService : IntentService("Quest proximity service") {
    // ...
    lateinit var mNotificationManager: NotificationManager

    override fun onHandleIntent(intent: Intent?) {



        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {

            Log.e("Geofence event:", "Failed")
            return
        }


        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger

            showNotification("Quest is near by", "Click to join a quest")
            // Send notification and log the transition details.

            Log.i("Quest", "Found")
        }else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            mNotificationManager.cancel(0)

        }
        else {
            // Log the error.
            Log.e("Geofence : ", getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition))
        }



    }

    fun showNotification(title: String, content: String) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("EnterQuest",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "YOUR_NOTIFICATION_CHANNEL_DISCRIPTION"
            mNotificationManager.createNotificationChannel(channel)
        }
        val mBuilder = NotificationCompat.Builder(applicationContext, "default")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                .setAutoCancel(true) // clear notification after click
        val intent = Intent(applicationContext, GeofenceTransitionsIntentService::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pi)
        mNotificationManager.notify(0, mBuilder.build())
    }
}