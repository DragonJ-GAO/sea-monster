package com.sea_monster.core.network;

import java.io.Serializable;

public interface ApiCallback<T extends Serializable> extends
		RequestCallback<T> {
}
