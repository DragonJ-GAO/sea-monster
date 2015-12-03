package me.add1.network;

import android.net.Uri;
import android.util.Log;


import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import me.add1.exception.BaseException;
import me.add1.network.packer.AbsEntityPacker;
import me.add1.network.parser.IEntityParser;

public abstract class ApiRequest<T extends Serializable> implements ApiCallback<T> {
    private Uri uri;
    private List<ParamPair> params;
    private String method;
    private String fileName;

    public ApiRequest(String method, Uri uri) {
        this.method = method;
        this.uri = uri;
    }

    public ApiRequest(String method, Uri uri, List<ParamPair> params) {
        this.method = method;
        this.uri = uri;
        this.params = params;
    }
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public List<ParamPair> getParams() {
        return params;
    }

    public void setParams(List<ParamPair> params) {
        this.params = params;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("URI:%1$s\n", uri.toString()));

        int i = 0;

        for (ParamPair pair : params) {
            stringBuilder.append(String.format("Params($1$d):%2$s\n", i++, pair.getValue().toString()));
        }

        return super.toString();
    }

    public AbstractHttpRequest<T> obtainRequest(IEntityParser<T> parser, final AuthType consumer) {
        return obtainRequest(parser, consumer, false);
    }

    public AbstractHttpRequest<T> obtainRequest(IEntityParser<T> parser, final AuthType consumer, boolean isMultiPart) {
        final ApiRequest<T> request = this;

        AbstractHttpRequest<T> httpRequest = new AbstractHttpRequest<T>(method, uri, params, AbstractHttpRequest.HIGH) {

            @Override
            public void processReadyRequest(HttpRequest request) {

                if (consumer != null)
                    try {
                        synchronized (consumer) {
                            consumer.signRequest(request, params);
                        }
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
            }

            @Override
            public void onComplete(T obj) {
                request.onComplete(this, obj);
            }

            @Override
            public void onFailure(BaseException e) {
                request.onFailure(this, e);
            }
        };
        httpRequest.setParser(parser);

        return httpRequest;
    }

    public AbstractHttpRequest<T> obtainRequest(IEntityParser<T> parser, AbsEntityPacker<?> packer,
                                                final AuthType consumer) {
        final ApiRequest<T> request = this;

        AbstractHttpRequest<T> httpRequest = new AbstractHttpRequest<T>(method, uri, params, AbstractHttpRequest.HIGH) {

            @Override
            public void processReadyRequest(HttpRequest request) {
                if (consumer != null)
                    try {

                        synchronized (consumer) {
                            consumer.signRequest(request, params);
                        }
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
            }


            @Override
            public void onComplete(T obj) {
                request.onComplete(this, obj);

            }

            @Override
            public void onFailure(BaseException e) {
                request.onFailure(this, e);
            }
        };
        httpRequest.setParser(parser);
        httpRequest.setPacker(packer);
        return httpRequest;
    }
}
