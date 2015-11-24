package com.sea_monster.network.parser;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;
import com.sea_monster.network.NameValuePair;
import com.sea_monster.network.StatusCallback;

public class NonResultParser implements IEntityParser<Boolean> {
    @Override
    public void onHeaderParsed(NameValuePair[] headers) {

    }

    @Override
	public Boolean parse(InputStream inputStream) throws IOException, ParseException, InternalException {
		inputStream.close();
		return true;
	}

	@Override
	public Boolean parse(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
		inputStream.close();
		return true;
	}

	@Override
	public Boolean parseGzip(InputStream inputStream) throws IOException, ParseException, InternalException {
		inputStream.close();
		return true;
	}

	@Override
	public Boolean parseGzip(InputStream inputStream, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
		inputStream.close();
		return true;
	}

}
