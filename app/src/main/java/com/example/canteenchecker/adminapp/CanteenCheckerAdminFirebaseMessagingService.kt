package com.example.canteenchecker.adminapp

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CanteenCheckerAdminFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // nothing to do since app does not use token based - nicht relevant für die HÜ
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data["canteenId"]?.let{ canteenId ->
            sendCanteenChangedBroadcast(canteenId)
        }
    }
}