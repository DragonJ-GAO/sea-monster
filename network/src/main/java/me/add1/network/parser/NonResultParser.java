package me.add1.network.parser;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

import me.add1.exception.InternalException;
import me.add1.exception.ParseException;
import me.add1.network.NameValuePair;
import me.add1.network.StatusCallback;

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
