package me.add1.test;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import me.add1.resource.ResourceHandler;

import java.io.File;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by DragonJ on 14/12/25.
 */
public class App extends Application {
    static App sS;

    public static App getInstance(){
        return sS;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        new ResourceHandler.Builder().enableBitmapCache().build(this);

        sS = this;
    }

    private final File getResourceDir(Context context) {


        File environmentPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            environmentPath = Environment.getExternalStorageDirectory();
            environmentPath = new File(environmentPath, "Android/data/" + context.getPackageName());
        } else {
            environmentPath = context.getFilesDir();
        }

        File baseDirectory = new File(environmentPath, "RongCloud");

        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }

        return baseDirectory;
    }
}
