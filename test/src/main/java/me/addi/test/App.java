package me.addi.test;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.sea_monster.core.common.Const;
import com.sea_monster.core.common.DiscardOldestPolicy;
import com.sea_monster.core.network.DefaultHttpHandler;
import com.sea_monster.core.network.HttpHandler;
import com.sea_monster.core.resource.ResourceManager;
import com.sea_monster.core.resource.cache.ResourceCacheWrapper;
import com.sea_monster.core.resource.compress.ResourceCompressHandler;
import com.sea_monster.core.resource.io.FileSysHandler;
import com.sea_monster.core.resource.io.IFileSysHandler;
import com.sea_monster.core.resource.io.ResourceRemoteWrapper;
import com.sea_monster.core.utils.FileUtils;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.senab.bitmapcache.BitmapLruCache;

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


        BlockingQueue<Runnable> mWorkQueue = new PriorityBlockingQueue<Runnable>(Const.SYS.WORK_QUEUE_MAX_COUNT);

        ThreadFactory mThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ConnectTask #" + mCount.getAndIncrement());
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(Const.SYS.DEF_THREAD_WORDER_COUNT, Const.SYS.MAX_THREAD_WORKER_COUNT, Const.SYS.CREATE_THREAD_TIME_SPAN,
                TimeUnit.SECONDS, mWorkQueue, mThreadFactory);

        executor.setRejectedExecutionHandler(new DiscardOldestPolicy());

        IFileSysHandler fileSysHandler = new FileSysHandler(executor, getResourceDir(this), "file", "rong");

        HttpHandler httpHandler = new DefaultHttpHandler(this, executor);

        ResourceRemoteWrapper remoteWrapper = new ResourceRemoteWrapper(this, fileSysHandler, httpHandler);

        File cacheFile = new File(getResourceDir(this), "cache");
        if (!cacheFile.exists())
            FileUtils.createDirectory(cacheFile, true);

        BitmapLruCache cache = new BitmapLruCache.Builder(this).setDiskCacheEnabled(true).setDiskCacheLocation(cacheFile).setMemoryCacheMaxSize(5 * 1024 * 1204).build();

        ResourceCompressHandler compressHandler = new ResourceCompressHandler(this, fileSysHandler);

        ResourceCacheWrapper cacheWrapper = new ResourceCacheWrapper(this, cache, fileSysHandler, compressHandler);

        ResourceManager.init(this, remoteWrapper, cacheWrapper);

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
