package cz.muni.irtis.screenshottest;

import android.os.Handler;
import android.util.Log;

public class TaskScheduler {
    private static final String TAG = TaskScheduler.class.getSimpleName();
    private final Handler delayHandler;
    private final int delay = 1000;
    Screenshot screenshot;

    public TaskScheduler() {
        delayHandler = new Handler();
    }

    public void addScreenshot(Screenshot screenshot) {
        this.screenshot = screenshot;
    }

    public void startCapturingPeriodicaly() {
        delayHandler.postDelayed(new Runnable() {
            public void run() {
                startCapture();
                delayHandler.postDelayed(this, delay);
            }
        }, delay);
    }
    public void onDestroy() {
        delayHandler.removeCallbacksAndMessages(null);
        screenshot.stop();
    }
    private void startCapture() {
        try {
            screenshot.run();
        } catch (Exception e) {
            Log.e(TAG, "Screenshot: " + e.toString());
        }
    }
}
