package com.alfisal.accounting

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        scheduleNext(context)
    }

    companion object {
        private const val CHANNEL_ID   = "alfisal_daily_v3"
        private const val NOTIF_ID     = 2001
        private const val REQUEST_CODE = 5001

        // ── وقت الإشعار: 5 مساءً ──────────────────────────────────────────
        private const val NOTIF_HOUR   = 17
        private const val NOTIF_MINUTE = 0

        fun showNotification(context: Context) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // نغمة مخصصة إذا وُجد ملف في res/raw/notification_sound وإلا الافتراضية
            val customSoundUri = getCustomSoundUri(context)
            val soundUri = customSoundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID, "تذكير يومي الفواتير", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكير بتسجيل الفواتير اليومية"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(soundUri, audioAttr)
                enableLights(true)
                lightColor = 0xFF0D9488.toInt()
            }
            nm.createNotificationChannel(channel)

            val openIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("الفيصل — تذكير يومي")
                .setContentText("لا تنسى تضيف الفواتير الجديدة 📋")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("لا تنسى تضيف الفواتير الجديدة\nسجّل مشترياتك ومبيعاتك ومصروفات اليوم الآن 📋"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 400, 200, 400))
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setColor(0xFF0D9488.toInt())
                .build()

            nm.notify(NOTIF_ID, notification)
        }

        private fun getCustomSoundUri(context: Context): Uri? {
            return try {
                val resId = context.resources.getIdentifier("notification_sound", "raw", context.packageName)
                if (resId != 0) Uri.parse("android.resource://${context.packageName}/$resId")
                else null
            } catch (e: Exception) { null }
        }

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getPendingIntent(context)

            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, NOTIF_HOUR)
                set(Calendar.MINUTE, NOTIF_MINUTE)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                pendingIntent
            )
        }

        private fun scheduleNext(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, NOTIF_HOUR)
                set(Calendar.MINUTE, NOTIF_MINUTE)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                getPendingIntent(context)
            )
        }

        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java)
            return PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendingIntent(context))
        }
    }
}
