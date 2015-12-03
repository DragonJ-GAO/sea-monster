package me.add1.network;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface AuthType {

	public void signRequest(HttpRequest httpRequest,
                            List<ParamPair> orgParams) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException;
}
