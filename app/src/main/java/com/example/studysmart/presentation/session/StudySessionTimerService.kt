package com.example.studysmart.presentation.session

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.studysmart.util.Constants.ACTION_SERVICE_CANCEL
import com.example.studysmart.util.Constants.ACTION_SERVICE_START
import com.example.studysmart.util.Constants.ACTION_SERVICE_STOP
import com.example.studysmart.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.studysmart.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.studysmart.util.Constants.NOTIFICATION_ID
import com.example.studysmart.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class StudySessionTimerService: Service() {

    @Inject
    lateinit var notificationManager: NotificationManager


    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder


    private val binder = StudySessionTimerBinder()

     private lateinit var timer: Timer


    var duration = mutableStateOf(Duration.ZERO)
        private set

    var seconds = mutableStateOf("00")
        private set

    var minutes = mutableStateOf("00")
        private set

    var hours = mutableStateOf("00")
        private set

    var currentTimerState = mutableStateOf(TimerState.IDLE)
        private set

    var subjectId = mutableStateOf<Int?>(null)
        private set

    fun setRelatedSubject(newSubjectId: Int?) {
        subjectId.value = newSubjectId
    }

    override fun onBind(p0: Intent?) = binder


    // --- Making existing methods public or creating public wrappers ---
    fun publicStartTimerTrigger() { // Public wrapper for UI to call
        val intent = Intent(this, StudySessionTimerService::class.java).apply {
            action = ACTION_SERVICE_START
        }
        startService(intent)
    }

    fun publicStopTimerTrigger() { // Public wrapper for UI to call (acts as Pause)
        val intent = Intent(this, StudySessionTimerService::class.java).apply {
            action = ACTION_SERVICE_STOP
        }
        startService(intent)
    }

    fun publicCancelTimerTrigger() { // Public wrapper for UI to call (acts as Stop & Reset)
        val intent = Intent(this, StudySessionTimerService::class.java).apply {
            action = ACTION_SERVICE_CANCEL
        }
        startService(intent)
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action.let {
            when(it) {

                ACTION_SERVICE_START -> {
                    startForegroundService()
                    startTimer { hours, minutes, seconds ->
                            updateNotification(hours,minutes,seconds)
                    }

                }
                ACTION_SERVICE_STOP -> {
                    stopTimer()
                }
                ACTION_SERVICE_CANCEL -> {
                       stopTimer()
                    cancelTimer()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
         createNotificationChannel()
          startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
         val channel = NotificationChannel(
             NOTIFICATION_CHANNEL_ID,
             NOTIFICATION_CHANNEL_NAME,
             NotificationManager.IMPORTANCE_LOW
         )

          notificationManager.createNotificationChannel(channel)

    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
         notificationManager.notify(
             NOTIFICATION_ID,
             notificationBuilder
                 .setContentText("$hours:$minutes:$seconds")
                 .build()
         )
    }

     private fun startTimer(
          onTick:(h: String, m: String, s: String)->Unit
     ) {

           currentTimerState.value = TimerState.STARTED
         timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
              duration.value = duration.value.plus(1.seconds)
               updateTimeUnits()
               onTick(hours.value,minutes.value,seconds.value)

         }
     }

    private fun stopTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        if (currentTimerState.value == TimerState.STARTED) { // Only change state if it was running
            currentTimerState.value = TimerState.STOPPED
        }
    }

    private fun cancelTimer() {
        duration.value = Duration.ZERO
        updateTimeUnits()
        subjectId.value = null
        currentTimerState.value = TimerState.IDLE
    }

    private fun updateTimeUnits() {
         duration.value.toComponents{ hours, minutes, seconds, _ ->
              this@StudySessionTimerService.hours.value = hours.toInt().pad()
              this@StudySessionTimerService.minutes.value = minutes.pad()
              this@StudySessionTimerService.seconds.value = seconds.pad()
         }
    }

    inner class  StudySessionTimerBinder: Binder() {
        fun getService(): StudySessionTimerService = this@StudySessionTimerService
    }

}





enum class TimerState {

    IDLE,
    STARTED,
    STOPPED



}