package com.sea_monster.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.sea_monster.network.StoreStatusCallback;

/**
 * Created by DragonJ on 15/1/27.
 */
public interface IResourceHandler<T> {
    public boolean exists(Resource resource);

    public T get(Resource resource);

    public File getFile(Resource resource);

    public InputStream getInputStream(Resource resource) throws IOException;

    public void store(Resource resource, InputStream is) throws IOException;

    public void store(Resource resource, InputStream is, long total, StoreStatusCallback statusCallback)
            throws IOException;

    public void cleanup();

    public void remove(Resource resource);
}
