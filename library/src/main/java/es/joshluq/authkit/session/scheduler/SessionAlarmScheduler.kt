package es.joshluq.authkit.session.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import es.joshluq.authkit.session.receiver.SessionAlarmReceiver

internal class SessionAlarmScheduler(
    private val context: Context
) : SessionScheduler {

    companion object {
        const val EXPIRATION_REQUEST_CODE = 1001
        const val WARNING_REQUEST_CODE = 1002
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(totalDuration: Long, warningBefore: Long?) {
        cancelAll()

        // Expiration Alarm
        scheduleAlarm(totalDuration, SessionAlarmReceiver.ACTION_SESSION_EXPIRATION, EXPIRATION_REQUEST_CODE)

        // Warning Alarm (optional)
        warningBefore?.let {
            val warningDelay = totalDuration - it
            if (warningDelay > 0) {
                scheduleAlarm(warningDelay, SessionAlarmReceiver.ACTION_SESSION_WARNING, WARNING_REQUEST_CODE)
            }
        }
    }

    private fun scheduleAlarm(delay: Long, action: String, requestCode: Int) {
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + delay

        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun cancelAll() {
        val alarms = listOf(
            EXPIRATION_REQUEST_CODE to SessionAlarmReceiver.ACTION_SESSION_EXPIRATION,
            WARNING_REQUEST_CODE to SessionAlarmReceiver.ACTION_SESSION_WARNING
        )
        alarms.forEach { (code, action) ->
            val intent = Intent(context, SessionAlarmReceiver::class.java).apply { this.action = action }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
