package com.sea_monster.core.common;

public abstract class PriorityRunnable implements Runnable,
		Comparable<PriorityRunnable> {

	public static final int NORMAL = 0;
	public static final int LOW = -1;
	public static final int HIGH = 1;
	public static final int VERY_HIGH = 2;

	private int priority = NORMAL;

	public PriorityRunnable() {
		this.priority = NORMAL;
	}

	public PriorityRunnable(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(PriorityRunnable another) {
		return another.getPriority() - this.priority;
	}

	public int getPriority() {
		return priority;
	}

}
