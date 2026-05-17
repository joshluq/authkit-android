package es.joshluq.authkit.session.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import es.joshluq.authkit.di.AuthKitLocator

internal class SessionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // Recover SessionKit via Service Locator
        val sessionKit = AuthKitLocator.resolveSessionKit()

        when (action) {
            ACTION_SESSION_EXPIRATION -> {
                // Direct call to mediator method
                sessionKit.onExpirationDetected()
            }
            ACTION_SESSION_WARNING -> {
                // Direct call to mediator method
                sessionKit.onPreExpirationDetected()
            }
        }
    }

    companion object {
        const val ACTION_SESSION_EXPIRATION = "es.joshluq.authkit.session.ACTION_SESSION_EXPIRATION"
        const val ACTION_SESSION_WARNING = "es.joshluq.authkit.session.ACTION_SESSION_WARNING"
    }
}
