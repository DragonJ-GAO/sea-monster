package me.add1.network;

import android.net.Uri;

import java.util.Iterator;

/**
 * Created by dragonj on 15/11/18.
 */
public interface HttpRequest {
    public static final int NORMAL = 0;
    public static final int LOW = -1;
    public static final int HIGH = 1;

    public final static String GET_METHOD = "GET";
    public final static String POST_METHOD = "POST";
    public final static String PUT_METHOD = "PUT";

    String getMethod();
    Uri getUri();

    boolean containsHeader(String var1);

    NameValuePair[] getHeaders(String var1);

    NameValuePair getFirstHeader(String var1);

    NameValuePair getLastHeader(String var1);

    NameValuePair[] getAllHeaders();

    void addHeader(NameValuePair var1);

    void addHeader(String var1, String var2);

    void setHeader(NameValuePair var1);

    void setHeader(String var1, String var2);

    void setHeaders(NameValuePair[] var1);

    void removeHeader(NameValuePair var1);

    void removeHeaders(String var1);

    Iterator<NameValuePair> headerIterator();

    boolean containsParams(String var1);

    ParamPair getParam(String var1);

    ParamPair[] getAllParams();

    void addParam(ParamPair var1);

    void addParam(String var1, String var2);

    void setParam(ParamPair var1);

    void setParam(String var1, String var2);

    void setParams(ParamPair[] var1);

    void removeParam(ParamPair var1);

    void removeParam(String var1);

    Iterator<ParamPair> paramIterator();




}
