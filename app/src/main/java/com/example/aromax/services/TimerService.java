package com.example.aromax.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.aromax.R;
import com.example.aromax.MainActivity;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    public static final String ACTION_TIMER_TICK = "com.example.aromax.TIMER_TICK";
    public static final String EXTRA_TIME_REMAINING = "time_remaining";
    public static final String ACTION_TIMER_FINISH = "com.example.aromax.TIMER_FINISH";
    public static final String EXTRA_DURATION_MS = "duration_ms";

    private static final String CHANNEL_ID = "TimerServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private CountDownTimer countDownTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long duration = intent.getLongExtra(EXTRA_DURATION_MS, 0);

        startForeground(NOTIFICATION_ID, createNotification("Iniciando..."));

        startTimer(duration);

        return START_STICKY;
    }

    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeFormatted = formatTime(millisUntilFinished);
                updateNotification(timeFormatted);
                broadcastUpdate(ACTION_TIMER_TICK, timeFormatted);
            }

            @Override
            public void onFinish() {
                updateNotification("Tempo finalizado!");
                broadcastUpdate(ACTION_TIMER_FINISH, "00:00");
                stopSelf(); // Para o serviço quando o timer termina
            }
        }.start();
    }

    private void broadcastUpdate(String action, String data) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_TIME_REMAINING, data);
        sendBroadcast(intent);
    }

    private String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }


    private Notification createNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Temporizador Ativo")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone seu
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true) // Evita sons/vibrações repetidas na atualização
                .build();
    }

    private void updateNotification(String text) {
        Notification notification = createNotification(text);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal do Serviço de Timer",
                    NotificationManager.IMPORTANCE_LOW // Use LOW para evitar som
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}