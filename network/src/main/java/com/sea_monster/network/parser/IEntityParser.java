package com.sea_monster.network.parser;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;

import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;
import com.sea_monster.network.StatusCallback;

public interface IEntityParser<T> {

    public void onHeaderParsed(Header[] headers);

	public T parse(HttpEntity entity) throws IOException, ParseException, InternalException;

	public T parse(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException;

	public T parseGzip(HttpEntity entity) throws IOException, ParseException, InternalException;

	public T parseGzip(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException;
}
