package com.sea_monster.core.common;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Observable {

	protected long mStatus;
	boolean mChanged = false;

	public static final byte STATIE_MASK = 3;
	public static final byte STATIE_EMPTY = 0;

	private List<Entity> mEnters = new ArrayList<Entity>(8);

	public void addListener(byte type, Observer observer) {
		synchronized (mEnters) {
			int index = entityIndex(observer);

			if (index == -1) {
				Entity entity = new Entity();
				entity.statusMask = setStatus(0l, type, STATIE_MASK);
				entity.mWeakObserver = new WeakReference<Observer>(observer);
				mEnters.add(entity);
			} else {
				Entity entity = mEnters.get(index);
				if (entity.mWeakObserver.isEnqueued()) {
					mEnters.remove(index);

					Entity newEntity = new Entity();
					newEntity.statusMask = setStatus(0l, type, STATIE_MASK);
					newEntity.mWeakObserver = new WeakReference<Observer>(observer);
					mEnters.add(newEntity);
				} else {
					entity.statusMask = setStatus(entity.statusMask, type, STATIE_MASK);
				}
			}
		}
	}

	public void removeListener(byte type, Observer observer) {
		synchronized (mEnters) {
			int index = entityIndex(observer);

			if (index == -1)
				return;

			else {
				Entity entity = mEnters.get(index);

				if (entity.mWeakObserver.isEnqueued()) {
					mEnters.remove(index);
				} else {
					entity.statusMask = setStatus(entity.statusMask, type, STATIE_EMPTY);
					if (entity.statusMask == 0)
						mEnters.remove(index);
				}
			}
		}
	}

	public void addListener(Observer observer) {
		synchronized (observer) {
			int index = entityIndex(observer);
			long allMash = ~0l;
			if (index == -1) {
				Entity newEntity = new Entity();
				newEntity.mWeakObserver = new WeakReference<Observer>(observer);
				newEntity.statusMask = allMash;
				mEnters.add(newEntity);
			} else {
				Entity entity = mEnters.get(index);
				if (entity.mWeakObserver.isEnqueued()) {
					mEnters.remove(index);

					Entity newEntity = new Entity();
					newEntity.mWeakObserver = new WeakReference<Observer>(observer);
					newEntity.statusMask = allMash;
					mEnters.add(newEntity);
				} else {
					entity.statusMask = allMash;
				}
			}
		}
	}

	public void removeListener(Observer observer) {
		synchronized (observer) {
			int index = entityIndex(observer);

			if (index != -1) {
				mEnters.remove(index);
			}
		}
	}

	private final int entityIndex(Observer observer) {
		if (mEnters.size() == 0)
			return -1;

		int i = -1;
		for (Entity entity : mEnters) {
			i++;
			if (!entity.mWeakObserver.isEnqueued() && entity.mWeakObserver.get() == observer) {
				return i;
			}
		}
		return -1;
	}

	class Entity {
		long statusMask;
		WeakReference<Observer> mWeakObserver;

		@Override
		public String toString() {
			return String.format("status:%1$s  Observer:%2$b %3$s", Long.toBinaryString(statusMask), mWeakObserver.isEnqueued(),
					mWeakObserver.isEnqueued() ? null : mWeakObserver.get().toString());
		}
	}

	public byte getCurrentStatue(int type) {
		return (byte) ((mStatus & (3 << type)) >> type);
	}

	private byte getStatus(long status, byte type) {
		return (byte) ((status & (3 << type)) >> type);
	}

	private long setStatus(long status, byte type, byte currentStatus) {
		status = (status & ~(3 << type)) | currentStatus << type;
		return status;
	}

	public boolean setCurrentStatus(byte type, byte currentStatus, Object data) {
		if (getCurrentStatue(type) == currentStatus)
			return false;

		mStatus = (mStatus & ~(3 << type)) | currentStatus << type;

		notifyObservers(type, currentStatus, data);
		return true;
	}

	public void notifyObservers(byte type, byte status, Object data) {

		List<Observer> observers = new ArrayList<Observer>();
		synchronized (this) {

			int index = 0;
			index = mEnters.size();

			while (--index >= 0) {
				Entity entity = mEnters.get(index);
				if (entity.mWeakObserver.get() == null || entity.mWeakObserver.isEnqueued())
					mEnters.remove(index);
				else {
					if (getStatus(entity.statusMask, type) != 0) {
						observers.add(entity.mWeakObserver.get());
					}
				}
			}

			if (observers.size() > 0) {
				for (Observer observer : observers) {
					observer.onNotify(this, type, status, data);
				}
			}
		}

	}

	protected void clearChanged() {
		mChanged = false;
	}

	public boolean hasChanged() {
		return mChanged;
	}

	protected void setChanged() {
		mChanged = true;
	}

	public void printDebugInfo(PrintStream stream) {
		stream.print("CURRENT_STATUS:");
		stream.println(Long.toBinaryString(mStatus));

		stream.println("Observers:");
		for (Entity entity : mEnters) {
			stream.println(entity.toString());
		}
	}
}
