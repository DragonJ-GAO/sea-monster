package me.add1.test;

/**
 * Created by tagore on 15/10/30.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * UncaughtExceptionHanlder 作用 : 处理 线程被未捕获的异常终止 的情况, 一旦出现了未捕获异常崩溃, 系统就会回调该类的
 * uncaughtException 方法;
 */
public class CrashHandler implements UncaughtExceptionHandler {
    Context mContext;

    public CrashHandler(Context act) {
        mContext = act;
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {

        sendCrashReport(ex);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {

        }

        handleException();

    }

    private void sendCrashReport(Throwable ex) {

        File file = mContext.getExternalFilesDir("crash");

        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            File outFile = new File(file, "" + SystemClock.elapsedRealtime() + ".log");
            outFile.createNewFile();
            saveCrashInfo2File(outFile, ex);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleException() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private void saveCrashInfo2File(File file, Throwable ex) throws IOException {

        StringBuffer sb = new StringBuffer();

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        Log.v("SDFSDFSDF", sb.toString());
        fos.close();
    }
}

