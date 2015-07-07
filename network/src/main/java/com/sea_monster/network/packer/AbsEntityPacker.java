package com.sea_monster.network.packer;


import org.apache.http.HttpEntity;
import org.json.JSONException;

import java.io.IOException;

import com.sea_monster.exception.InternalException;

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
