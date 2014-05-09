package com.sea_monster.core.network.parser;


import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.exception.ParseException;
import com.sea_monster.core.network.StatusCallback;

import org.apache.http.HttpEntity;

import java.io.IOException;

public class NonResultParser implements IEntityParser<Boolean> {

	@Override
	public Boolean parse(HttpEntity entity) throws IOException, ParseException, InternalException {
		entity.consumeContent();
		return true;
	}

	@Override
	public Boolean parse(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
		entity.consumeContent();
		return true;
	}

	@Override
	public Boolean parseGzip(HttpEntity entity) throws IOException, ParseException, InternalException {
		entity.consumeContent();
		return true;
	}

	@Override
	public Boolean parseGzip(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
		entity.consumeContent();
		return true;
	}

}
