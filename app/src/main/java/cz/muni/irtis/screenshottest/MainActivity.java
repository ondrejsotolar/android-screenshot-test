package cz.muni.irtis.screenshottest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private static final int SCREENSHOT_REQUEST_CODE = 59706;
    private MediaProjectionManager projectionMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButtons();

        if (!SchedulerService.IS_RUNNING) {
            createScreenCaptureIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCREENSHOT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(this, SchedulerService.class)
                        .putExtra(SchedulerService.EXTRA_RESULT_CODE, resultCode)
                        .putExtra(SchedulerService.EXTRA_RESULT_INTENT, data);
                SchedulerService.startRunning(this, i);
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                Log.w("MainActivity", "Screenshot permission not granted");
            }
        }
    }

    private void createScreenCaptureIntent() {
        projectionMgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionMgr.createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE);
    }

    private void initButtons() {
        Button stop = findViewById(R.id.stop_screen);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTakingMetrics();
            }
        });
    }

    private void stopTakingMetrics() {
        if (SchedulerService.IS_RUNNING) {
            Intent stopIntent = new Intent(this, SchedulerService.class);
            SchedulerService.stopRunning(this, stopIntent);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("LOW MEMORY", "");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e("TRIM MEMORY", String.valueOf(level));
    }
}
