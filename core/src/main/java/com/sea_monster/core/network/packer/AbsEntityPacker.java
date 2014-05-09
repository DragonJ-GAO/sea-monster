package com.sea_monster.core.network.packer;


import com.sea_monster.core.exception.InternalException;

import org.apache.http.HttpEntity;
import org.json.JSONException;

import java.io.IOException;

public abstract class AbsEntityPacker<T> {
    protected T obj;

    public AbsEntityPacker(T model) {
        this.obj = model;
    }

    public AbsEntityPacker() {

    }

    public T getModel() {
        return obj;
    }

    public abstract HttpEntity pack() throws IOException, InternalException, JSONException;

}
