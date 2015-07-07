package com.sea_monster.resource;


import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;
import com.sea_monster.network.StatusCallback;
import com.sea_monster.network.StoreStatusCallback;
import com.sea_monster.network.parser.IEntityParser;

public class ResParser implements IEntityParser<File> {

    private Resource res;
    private ResourceHandler handler;

    @Override
    public void onHeaderParsed(Header[] headers) {

    }

    @Override
    public File parse(HttpEntity entity) throws IOException, ParseException {

        return parseEntityStream(handler.getResourceHandler(entity), entity.getContent());
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
    public File parse(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException {
        File file;
        if (callback instanceof StoreStatusCallback)
            file = parseEntityStream(handler.getResourceHandler(entity), entity.getContent(), entity.getContentLength(), (StoreStatusCallback) callback);
        else
            file = parseEntityStream(handler.getResourceHandler(entity), entity.getContent());
        entity.consumeContent();
        return file;
    }

    @Override
    public File parseGzip(HttpEntity entity) throws IOException, ParseException, InternalException {
        return parseEntityStream(handler.getResourceHandler(entity), new GZIPInputStream(entity.getContent()));
    }

    @Override
    public File parseGzip(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
        File file;
        if (callback instanceof StoreStatusCallback)
            file = parseEntityStream(handler.getResourceHandler(entity), new GZIPInputStream(entity.getContent()), entity.getContentLength(), (StoreStatusCallback) callback);
        else
            file = parseEntityStream(handler.getResourceHandler(entity), new GZIPInputStream(entity.getContent()));
        entity.consumeContent();
        return file;
    }


}
