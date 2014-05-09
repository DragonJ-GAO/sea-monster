package com.sea_monster.core.network.parser;


import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.exception.ParseException;
import com.sea_monster.core.network.StatusCallback;

import org.apache.http.HttpEntity;

import java.io.IOException;

public interface IEntityParser<T> {

	public T parse(HttpEntity entity) throws IOException, ParseException, InternalException;

	public T parse(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException;

	public T parseGzip(HttpEntity entity) throws IOException, ParseException, InternalException;

	public T parseGzip(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException;
}
