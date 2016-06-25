package me.add1.resource;

import java.io.IOException;
import java.io.InputStream;

import me.add1.network.StoreStatusCallback;

/**
 * Created by DragonJ on 15/5/25.
 */
public class ProgressInputStreamWrapper extends InputStream {

    InputStream inputStream;
    long total;
    StoreStatusCallback callback;
    StoreStatusCallback.StoreStatus status;

    public ProgressInputStreamWrapper(InputStream inputStream, long total, StoreStatusCallback callback) throws IOException{
        super();

        if (inputStream == null)
            throw new IOException();

        this.total = total;
        this.callback = callback;
        this.status = new StoreStatusCallback.StoreStatus(total);
        this.inputStream = inputStream;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        int count =  inputStream.read(buffer);
        status.addReceivedSize(count);
        if(callback!=null)
            callback.statusCallback(status);
        return count;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int count =  inputStream.read(buffer, byteOffset, byteCount);
        status.addReceivedSize(count);
        if(callback!=null)
            callback.statusCallback(status);
        return count;
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
        status = new StoreStatusCallback.StoreStatus(total);
        if(callback!=null)
            callback.statusCallback(status);
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return inputStream.skip(byteCount);
    }

    @Override
    public int read() throws IOException {
        int data = inputStream.read();
        status.addReceivedSize(1);
        if(callback!=null)
            callback.statusCallback(status);
        return data;
    }
}
