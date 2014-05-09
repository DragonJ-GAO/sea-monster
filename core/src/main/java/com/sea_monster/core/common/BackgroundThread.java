package com.sea_monster.core.common;

import android.os.Process;

/**
 * Created by dragonj on 11/4/13.
 */
public abstract class BackgroundThread implements Runnable {
    public final void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        runImpl();
    }

    public abstract void runImpl();

    protected boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

}
