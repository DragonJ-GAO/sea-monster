package me.add1.network.packer;


import me.add1.exception.PackException;

import java.io.InputStream;

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

    public abstract InputStream getContent() throws PackException;

    public abstract long getLength();

}
