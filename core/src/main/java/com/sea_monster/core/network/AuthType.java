package com.sea_monster.core.network;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface AuthType {

	public void signRequest(HttpRequest httpRequest,
                            List<NameValuePair> orgParams) throws UnsupportedEncodingException,
			NoSuchAlgorithmException, InvalidKeyException;
}
