package me.add1.network;

import java.io.Serializable;

public interface ApiCallback<T extends Serializable> extends
		RequestCallback<T> {
}
