package com.sea_monster.network.parser;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;

import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;
import com.sea_monster.network.StatusCallback;

public class NonResultParser implements IEntityParser<Boolean> {
    @Override
    public void onHeaderParsed(Header[] headers) {

    }

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
