package com.sea_monster.resource;


import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;
import com.sea_monster.network.NameValuePair;
import com.sea_monster.network.StatusCallback;
import com.sea_monster.network.StoreStatusCallback;
import com.sea_monster.network.parser.IEntityParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class ResParser implements IEntityParser<File> {

    private Resource res;
    private ResourceHandler handler;
    private String contentType;
    private long contentLength;

    @Override
    public void onHeaderParsed(NameValuePair[] headers) {
        if (headers == null)
            return;

        for (NameValuePair item : headers) {
            if (item.getName().equals("Content-Type"))
                contentType = item.getValue();
            else if (item.getName().equals("Content-Length"))
                contentLength = Long.parseLong(item.getValue());
        }
    }

    @Override
    public File parse(InputStream inputStream) throws IOException, ParseException {

        return parseEntityStream(handler.getResourceHandler(contentType), inputStream);
    }

    public ResParser(ResourceHandler handler, Resource res) {
        this.res = res;
        this.handler = handler;
    }

    private File parseEntityStream(IResourceHandler handler, InputStream inputStream, long total, StoreStatusCallback callback) throws IOException {
        handler.store(res, inputStream, total, callback);
        return handler.getFile(res);

    }

    private File parseEntityStream(IResourceHandler handler, InputStream inputStream) throws IOException {

        handler.store(res, inputStream);
        return handler.getFile(res);
    }


    @Override
    public File parse(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException {
        File file;
        if (callback instanceof StoreStatusCallback && contentLength > 0)
            file = parseEntityStream(handler.getResourceHandler(contentType), inputStream, contentLength, (StoreStatusCallback) callback);
        else
            file = parseEntityStream(handler.getResourceHandler(contentType), inputStream);
        inputStream.close();
        return file;
    }

    @Override
    public File parseGzip(InputStream inputStream) throws IOException, ParseException, InternalException {
        return parseEntityStream(handler.getResourceHandler(contentType), new GZIPInputStream(inputStream));
    }

    @Override
    public File parseGzip(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
        File file;
        if (callback instanceof StoreStatusCallback && contentLength > 0)
            file = parseEntityStream(handler.getResourceHandler(contentType), new GZIPInputStream(inputStream), contentLength, (StoreStatusCallback) callback);
        else
            file = parseEntityStream(handler.getResourceHandler(contentType), new GZIPInputStream(inputStream));
        inputStream.close();
        return file;
    }


}
