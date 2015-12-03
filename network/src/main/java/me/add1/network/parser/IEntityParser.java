package me.add1.network.parser;


import me.add1.exception.InternalException;
import me.add1.exception.ParseException;
import me.add1.network.NameValuePair;
import me.add1.network.StatusCallback;

import java.io.IOException;
import java.io.InputStream;

public interface IEntityParser<T> {

    public void onHeaderParsed(NameValuePair[] headers);

    public T parse(InputStream inputStream) throws IOException, ParseException, InternalException;

    public T parse(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException;

    public T parseGzip(InputStream inputStream) throws IOException, ParseException, InternalException;

    public T parseGzip(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException;
}
