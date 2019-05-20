package cz.muni.irtis.screenshottest;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class SchedulerService extends Service {
    private static final String TAG = SchedulerService.class.getSimpleName();

    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";
    public static boolean IS_RUNNING = false;
    private static final int CHANNEL_ID = 1337;
    private static Intent screenshotData;
    private NotificationBuilder notificationBuilder;
    private TaskScheduler taskScheduler;

    public static void startRunning(Context context, Intent screenshotIntent) {
        if (!IS_RUNNING) {
            Intent i = new Intent(context, SchedulerService.class);
            screenshotData = screenshotIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else {
                context.startService(i);
            }
            IS_RUNNING = true;
        } else {
            Log.d(TAG, "Already running.");
        }
    }

    public static void stopRunning(Context context, Intent serviceName) {
        if (IS_RUNNING) {
            context.stopService(serviceName);
        } else {
            Log.d(TAG, "Trying to stop not running service.");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskScheduler = new TaskScheduler();
        initMetrics();
        notificationBuilder = new NotificationBuilder(
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.initNotificationChannels();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(CHANNEL_ID,
                notificationBuilder.buildForegroundNotification(getApplicationContext()));
        taskScheduler.startCapturingPeriodicaly();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        taskScheduler.onDestroy();
        IS_RUNNING = false;
        stopForeground(false);
        Log.w(TAG, "service exiting.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        throw new IllegalStateException("Non-bindable service");
    }

    private void initMetrics() {
        if (screenshotData != null) {
            int resultCode = screenshotData.getIntExtra(EXTRA_RESULT_CODE, 1337);
            Intent resultData = screenshotData.getParcelableExtra(EXTRA_RESULT_INTENT);
            taskScheduler.addScreenshot(new Screenshot(getApplicationContext(), resultCode, resultData));
        } else {
            Log.e(TAG, "screenshot permission data is null");
        }
    }
}
