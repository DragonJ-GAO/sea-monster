package me.add1.resource;


import me.add1.exception.InternalException;
import me.add1.network.HttpHandler;

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

			if (runnable != null && runnable instanceof HttpHandler.PriorityRequestRunnable) {
				((HttpHandler.PriorityRequestRunnable<?>) runnable).getRequest().onFailure(
						new InternalException(InternalException.DISCARD_TASK, "rejectedExecution:oldest request Discard"));
			}

			e.execute(r);
		}
	}
}
