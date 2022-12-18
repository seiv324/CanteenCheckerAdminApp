package com.example.canteenchecker.adminapp

import android.app.Application

class CanteenCheckerAdminApplication : Application() {
    var authenticationToken: String? = null
    // TODO: Prüfen ob Token noch gültig ist -> function die null oder token retourniert

    override fun onCreate() {
        super.onCreate()

        //FirebaseMessaging.getInstance().subscribeToTopic("CanteenUpdates")
    }
}