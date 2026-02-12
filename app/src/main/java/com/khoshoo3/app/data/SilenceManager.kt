package com.khoshoo3.app.data

import android.app.NotificationManager
import android.content.Context

/**
 * Manages Do Not Disturb (DND) mode via [NotificationManager].
 *
 * IMPORTANT: Always checks [isNotificationPolicyAccessGranted] before
 * attempting to change settings — otherwise the app will crash.
 */
object SilenceManager {

    fun enableDnd(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return false
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        return true
    }

    fun disableDnd(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return false
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        return true
    }

    fun isDndActive(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    fun isNotificationPolicyGranted(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }
}
