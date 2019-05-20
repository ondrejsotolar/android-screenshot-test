package cz.muni.irtis.screenshottest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.joda.time.DateTime;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class Screenshot {
    private static final String TAG = Screenshot.class.getSimpleName();

    private final int VIRT_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private final HandlerThread handlerThread =
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);

    private Handler handler;
    private int resultCode;
    private Intent resultData;
    private MediaProjectionManager mediaProjectionManager;
    private WindowManager windowManager;
    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageTransmogrifier imageTransmogrifier;
    private String imagePath;
    private int width;
    private int height;
    private Context context;



    /**
     * Constructor.
     * Init system services, handler thread, get permission from params.
     * @param context context
     * @param params 0: resultCode, 1: resultIntent - special permission for media projection API
     */
    public Screenshot(Context context, Object... params) {
        this.context = context;

        mediaProjectionManager =
                (MediaProjectionManager) getContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);

        resultCode = (int) params[0];
        resultData = (Intent) params[1];

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        initScreenSize();
    }

    /**
     * Run the media projection.
     * Register callback with the image saver (ImageTransmogrifier).
     */
    public void run() {
        try {
            if (projection == null) {
                projection = mediaProjectionManager.getMediaProjection(resultCode, resultData);


            }
        } catch(IllegalStateException e) {
            Log.w(TAG, "Screenshot skipped: Cannot start already started MediaProjection");
            return;
        }
        imageTransmogrifier = new ImageTransmogrifier(this);
        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                virtualDisplay.release();
            }
        };
        if (projection != null && imageTransmogrifier != null) {
            virtualDisplay = projection.createVirtualDisplay(
                    getClass().getSimpleName(),
                    imageTransmogrifier.getWidth(),
                    imageTransmogrifier.getHeight(),
                    getContext().getResources().getDisplayMetrics().densityDpi,
                    VIRT_DISPLAY_FLAGS,
                    imageTransmogrifier.getSurface(),
                    null,
                    handler);
            projection.registerCallback(callback, handler);
        }

        //projection.stop();
    }

    public void stop() {
        if (projection != null) {
            projection.stop();
            if (virtualDisplay != null) {
                virtualDisplay.release();
            }
            projection = null;
        }
        //super.stop();
    }

    /**
     * Save the image URL & clean virtual display resources.
     * @param imagePath absolute path to image
     */
    public void finishCapture(String imagePath) {
//        if (projection != null) {
//            projection.stop();
//            if (virtualDisplay != null) {
//                virtualDisplay.release();
//            }
//            projection = null;
//        }
//        if (imageTransmogrifier != null) {
//            imageTransmogrifier.close();
//        }
//        if (imagePath != null && !"".equals(imagePath)) {
//            this.imagePath = imagePath;
//            save(DateTime.now());
//        }
    }

    public Context getContext() {
        return context;
    }

    public String getUrl() {
        return imagePath;
    }

    public Handler getHandler() {
        return handler;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void initScreenSize() {
        if (width > 0 && height > 0) {
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);
        width = size.x;
        height = size.y;

        while (width * height > (2 << 19)) {
            width = width >> 1;
            height = height >> 1;
        }
    }
}
