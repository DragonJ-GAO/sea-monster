package me.add1.network;

import android.net.Uri;

import me.add1.common.PriorityRunnable;
import me.add1.network.packer.AbsEntityPacker;
import me.add1.network.parser.IEntityParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class AbstractHttpRequest<T> implements HttpRequest, HttpRequestProcess<T> {

    private String method;
    private int callId;
    private Uri uri;
    private List<NameValuePair> headers;
    private List<ParamPair> params;
    private int priority;
    private IEntityParser<?> parser;
    private AbsEntityPacker<?> packer;

    private StatusCallback<?> statusCallback;

    public StatusCallback<?> getStatusCallback() {
        return statusCallback;
    }

    public void setStatusCallback(StatusCallback<?> statusCallback) {
        this.statusCallback = statusCallback;
    }


    public AbstractHttpRequest(String method, Uri uri, List<NameValuePair> headers, List<ParamPair> params, AbsEntityPacker packer, IEntityParser parser, int priority) {
        this.callId = new Random().nextInt();
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.params = params;
        this.packer = packer;
        this.parser = parser;
        this.priority = priority;

    }

    public AbstractHttpRequest(String method, Uri uri, List<ParamPair> params, IEntityParser<?> parser) {
        this(method, uri, null, params, null, parser, PriorityRunnable.NORMAL);

    }

    public AbstractHttpRequest(String method, Uri uri, List<ParamPair> params) {
        this(method, uri, null, params, null, null, PriorityRunnable.NORMAL);
    }

    public AbstractHttpRequest(String method, Uri uri, List<ParamPair> params, IEntityParser<?> parser, int priority) {
        this(method, uri, null, params, null, parser, priority);
    }

    public AbstractHttpRequest(String method, Uri uri, List<ParamPair> params, int priority) {
        this(method, uri, null, params, null, null, priority);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("CallId:%1$d\n", callId));
        stringBuilder.append(String.format("URI:%1$s\n", uri.toString()));

        int i = 0;

        if (headers != null && headers.size() > 0) {
            stringBuilder.append('\n');
            for (NameValuePair item : headers)
                stringBuilder.append(String.format("Header($1$d):%2$s\n", item.getName(), item.getValue()));
        }
        if (params != null && params.size() > 0) {
            stringBuilder.append('\n');
            for (ParamPair item : params) {
                stringBuilder.append(String.format("Params($1$d):%2$s\n", item.getName(), item.getValue()));
            }
        }

        return super.toString();
    }

    public int getCallId() {
        return callId;
    }

    public int getPriority() {
        return this.priority;
    }


    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public IEntityParser<?> getParser() {
        return parser;
    }

    public void setParser(IEntityParser<?> parser) {
        this.parser = parser;
    }

    public AbsEntityPacker<?> getPacker() {
        return packer;
    }

    public void setPacker(AbsEntityPacker<?> packer) {
        this.packer = packer;
    }

    @Override
    public boolean containsHeader(String var1) {
        if (headers == null)
            return false;

        for (NameValuePair item : headers)
            if (item.getName().equals(var1))
                return true;

        return false;
    }

    @Override
    public NameValuePair[] getHeaders(String var1) {
        if (headers == null)
            return null;

        List<NameValuePair> result = new ArrayList<>();

        for (NameValuePair item : headers)
            if (item.getName().equals(var1))
                result.add(item);

        if (result.size() == 0)
            return null;

        return (NameValuePair[]) headers.toArray();
    }

    @Override
    public NameValuePair getFirstHeader(String var1) {
        if (headers == null)
            return null;

        for (NameValuePair item : headers)
            if (item.getName().equals(var1))
                return item;

        return null;
    }

    @Override
    public NameValuePair getLastHeader(String var1) {
        if (headers == null)
            return null;

        NameValuePair result = null;

        for (NameValuePair item : headers)
            if (item.getName().equals(var1))
                result = item;

        return result;
    }

    @Override
    public NameValuePair[] getAllHeaders() {
        if (headers == null)
            return null;
        return (NameValuePair[]) headers.toArray();
    }

    @Override
    public void addHeader(NameValuePair var1) {
        if (headers == null)
            headers = new ArrayList<>();
        headers.add(var1);
    }

    @Override
    public void addHeader(String var1, String var2) {
        if (headers == null)
            headers = new ArrayList<>();
        headers.add(new NameValuePair(var1, var2));
    }

    @Override
    public void setHeader(NameValuePair var1) {
        if (var1 == null)
            return;

        if (headers == null)
            headers = new ArrayList<>();

        int index = 0;

        while (index < headers.size()) {
            if (headers.get(index).getName().equals(var1.getName())) {
                headers.remove(index);
                headers.add(index, var1);
                break;
            }
        }
    }

    @Override
    public void setHeader(String var1, String var2) {
        setHeader(new NameValuePair(var1, var2));
    }

    @Override
    public void setHeaders(NameValuePair[] var1) {
        if (var1 == null)
            return;

        if (headers == null)
            headers = new ArrayList<>();

        for (NameValuePair item : var1)
            headers.add(item);
    }

    @Override
    public void removeHeader(NameValuePair var1) {

        if (var1 == null)
            return;

        if (headers == null)
            return;

        int index = 0;

        while (index < headers.size()) {
            if (headers.get(index).getName().equals(var1.getName())) {
                headers.remove(index);
                break;
            }
        }
    }

    @Override
    public void removeHeaders(String var1) {
        if (var1 == null)
            return;

        if (headers == null)
            return;

        int index = 0;

        while (index < headers.size()) {
            if (headers.get(index).getName().equals(var1)) {
                headers.remove(index);
                break;
            }
        }
    }

    @Override
    public Iterator<NameValuePair> headerIterator() {
        if (headers == null)
            return null;

        return headers.iterator();
    }

    @Override
    public boolean containsParams(String var1) {
        if (headers == null)
            return false;

        for (ParamPair item : params)
            if (item.getName().equals(var1))
                return true;

        return false;
    }

    @Override
    public ParamPair getParam(String var1) {
        if (params == null)
            return null;

        List<ParamPair> result = new ArrayList<>();

        for (ParamPair item : params)
            if (item.getName().equals(var1))
                return item;

        return null;
    }

    @Override
    public ParamPair[] getAllParams() {
        if (params == null)
            return null;

        ParamPair[] paramPairs = new ParamPair[params.size()];
        int index= 0 ;
        for (ParamPair item : params){
            paramPairs[index++] = item;
        }

        return paramPairs;
    }

    @Override
    public void addParam(ParamPair var1) {
        if (params == null)
            params = new ArrayList<>();
        params.add(var1);
    }

    @Override
    public void addParam(String var1, String var2) {
        if (params == null)
            params = new ArrayList<>();
        params.add(new ParamPair(var1, var2));
    }

    @Override
    public void setParam(ParamPair var1) {
        if (var1 == null)
            return;

        if (params == null)
            params = new ArrayList<>();

        int index = 0;

        while (index < params.size()) {
            if (params.get(index).getName().equals(var1.getName())) {
                params.remove(index);
                params.add(index, var1);
                break;
            }
        }
    }

    @Override
    public void setParam(String var1, String var2) {
        setParam(new ParamPair(var1, var2));
    }

    @Override
    public void setParams(ParamPair[] var1) {
        if (var1 == null)
            return;

        if (params == null)
            params = new ArrayList<>();

        for (ParamPair item : var1)
            params.add(item);
    }

    @Override
    public void removeParam(ParamPair var1) {
        if (var1 == null)
            return;

        if (params == null)
            return;

        int index = 0;

        while (index < params.size()) {
            if (params.get(index).getName().equals(var1.getName())) {
                params.remove(index);
                break;
            }
        }
    }

    @Override
    public void removeParam(String var1) {
        if (var1 == null)
            return;

        if (params == null)
            return;

        int index = 0;

        while (index < params.size()) {
            if (params.get(index).getName().equals(var1)) {
                params.remove(index);
                break;
            }
        }
    }

    @Override
    public Iterator<ParamPair> paramIterator() {
        if (params == null)
            return null;

        return params.iterator();
    }
}
