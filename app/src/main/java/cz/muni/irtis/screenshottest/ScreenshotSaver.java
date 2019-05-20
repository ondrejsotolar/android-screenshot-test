package cz.muni.irtis.screenshottest;

import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.io.File;
import java.io.FileOutputStream;


public class ScreenshotSaver {
    private static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyyMMddHHmmssSSS")
            .toFormatter();


    public static String processImage_Threaded(final byte[] png, File externalFilesDir) {
        final String fileName = formatter.print(DateTime.now()) + ".png";
        final File output = new File(externalFilesDir, fileName);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FileOutputStream fos=new FileOutputStream(output);
                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();
                    Log.i("INFO: ", output.getAbsolutePath());
                }
                catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
                return null;
            }
        }.execute();
        return output.getAbsolutePath();
    }
}
