package com.sea_monster.core.common;


import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.network.DefaultHttpHandler;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class DiscardOldestPolicy implements RejectedExecutionHandler {

	public DiscardOldestPolicy() {
	}

	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		if (!e.isShutdown()) {
			Runnable runnable = null;
			if (e.getQueue() instanceof PriorityBlockingQueue || e.getQueue() instanceof PriorityQueue) {
				Iterator<Runnable> iterator = e.getQueue().iterator();

				while (iterator.hasNext()) {
					runnable = iterator.next();
				}

				if (runnable != null)
					e.getQueue().remove(runnable);
			} else {
				runnable = e.getQueue().poll();
			}

			if (runnable != null && runnable instanceof DefaultHttpHandler.PriorityRequestRunnable) {
				((DefaultHttpHandler.PriorityRequestRunnable<?>) runnable).getRequest().cancelRequest(
						new InternalException(InternalException.DISCARD_TASK, "rejectedExecution:oldest request Discard"));
			}

			e.execute(r);
		}
	}
}
