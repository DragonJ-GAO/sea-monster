package me.add1.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.add1.network.StoreStatusCallback;

/**
 * Created by DragonJ on 15/1/27.
 */
public interface IResourceHandler<T, K extends Resource> {
    public boolean exists(K resource);

    public T get(K resource);

    public File getFile(K resource);

    public InputStream getInputStream(K resource) throws IOException;

    public void store(K resource, InputStream is) throws IOException;

    public void store(K resource, InputStream is, long total, StoreStatusCallback statusCallback)
            throws IOException;

    public void cleanup();

    public void remove(K resource);
}
