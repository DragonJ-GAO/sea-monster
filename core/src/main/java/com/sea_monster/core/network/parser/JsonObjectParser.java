package com.sea_monster.core.network.parser;


import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.exception.ParseException;
import com.sea_monster.core.network.StatusCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;


public abstract class JsonObjectParser<T> implements IEntityParser<T> {
    public abstract T jsonParse(JsonReader reader) throws MalformedJsonException, JSONException, IOException,
            ParseException, InternalException;

    public final T parse(HttpEntity entity) throws IOException, ParseException, InternalException {
        JsonReader jReader = new JsonReader(new InputStreamReader(entity.getContent(), HTTP.UTF_8));

        try {
            return jsonParse(jReader);
        } catch (MalformedJsonException e) {
            throw new ParseException(e);
        } catch (JSONException e) {
            throw new ParseException(e);
        } catch (IllegalStateException e) {
            throw new ParseException(e);
        } finally {
            entity.consumeContent();
        }

    }

    public final T parse(InputStream inputStream) throws IOException, ParseException, InternalException {
        JsonReader jReader = new JsonReader(new InputStreamReader(inputStream, HTTP.UTF_8));

        try {
            return jsonParse(jReader);
        } catch (MalformedJsonException e) {
            throw new ParseException(e);
        } catch (JSONException e) {
            throw new ParseException(e);
        } catch (IllegalStateException e) {
            throw new ParseException(e);
        } catch (JsonParseException e) {
            e.printStackTrace();
            throw new ParseException(e);
        } finally {
            inputStream.close();
        }

    }

    public final T parse(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException,
            InternalException {
        return this.parse(entity);
    }

    @Override
    public final T parseGzip(HttpEntity entity) throws IOException, ParseException, InternalException {
        JsonReader jReader = new JsonReader(new InputStreamReader(new GZIPInputStream(entity.getContent()), HTTP.UTF_8));
        try {
            return jsonParse(jReader);
        } catch (JSONException e) {
            throw new ParseException(e);
        } finally {
            entity.consumeContent();
        }
    }

    @Override
    public final T parseGzip(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException,
            InternalException {
        // TODO Auto-generated method stub
        return parseGzip(entity);
    }

}