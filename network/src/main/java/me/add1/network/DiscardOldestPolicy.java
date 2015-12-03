package me.add1.network;


import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import me.add1.exception.InternalException;
import me.add1.network.apache.ApacheHttpHandler;

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

			if (runnable != null && runnable instanceof ApacheHttpHandler.PriorityRequestRunnable) {
				((ApacheHttpHandler.PriorityRequestRunnable<?>) runnable).getRequest().onFailure(
						new InternalException("rejectedExecution:oldest request Discard"));
			}

			e.execute(r);
		}
	}
}
