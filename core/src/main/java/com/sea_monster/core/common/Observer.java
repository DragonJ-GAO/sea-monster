package com.sea_monster.core.common;

public interface Observer {
	public void onNotify(Observable observable, byte statusType, byte status,
                         Object data);
}
