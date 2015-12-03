package me.add1.network;

import android.content.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import me.add1.exception.BaseException;
import me.add1.network.apache.ApacheHttpHandler;

/**
 * Created by DragonJ on 15/2/2.
 */
public class NetworkManager{
    static NetworkManager sS;
    HttpHandler mHandler;

    private NetworkManager(Context context, HttpHandler handler) {


        BlockingQueue<Runnable> mWorkQueue = new PriorityBlockingQueue<Runnable>(Const.WORK_QUEUE_MAX_COUNT);

        ThreadFactory mThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ConnectTask #" + mCount.getAndIncrement());
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(Const.DEF_THREAD_WORDER_COUNT, Const.MAX_THREAD_WORKER_COUNT, Const.CREATE_THREAD_TIME_SPAN,
                TimeUnit.SECONDS, mWorkQueue, mThreadFactory);

        executor.setRejectedExecutionHandler(new DiscardOldestPolicy());

        if(handler == null)
            handler = new ApacheHttpHandler(context, executor);

        mHandler = handler;
    }

    public static void init(Context context){
        sS = new NetworkManager(context, null);
    }

    public static NetworkManager getInstance()
    {
        return sS;
    }

    public <T> void requestAsync(AbstractHttpRequest<T> request){
        mHandler.executeRequest(request);
    }

    public void cancelRequest(AbstractHttpRequest<?> request){

    }



}
