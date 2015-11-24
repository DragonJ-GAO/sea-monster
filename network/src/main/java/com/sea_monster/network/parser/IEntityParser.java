package com.sea_monster.network.parser;


import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;
import com.sea_monster.network.NameValuePair;
import com.sea_monster.network.StatusCallback;

import java.io.IOException;
import java.io.InputStream;

public interface IEntityParser<T> {

    public void onHeaderParsed(NameValuePair[] headers);

    public T parse(InputStream inputStream) throws IOException, ParseException, InternalException;

    public T parse(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException;

    public T parseGzip(InputStream inputStream) throws IOException, ParseException, InternalException;

    public T parseGzip(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException;
}
