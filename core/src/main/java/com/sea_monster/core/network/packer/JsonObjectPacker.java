package com.sea_monster.core.network.packer;

import com.google.gson.stream.JsonWriter;
import com.sea_monster.core.exception.InternalException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;


public abstract class JsonObjectPacker<T extends Serializable> extends AbsEntityPacker<T>
{
	public JsonObjectPacker(T model)
	{
		super(model);
	}
	
	public JsonObjectPacker()
	{
		
	}

	private JsonWriter jsonWriter;

	public abstract void pack(JsonWriter jsonWriter, T object) throws JSONException, IOException, InternalException;

	protected final void packData(T object, JsonWriter jsonWriter) throws IOException, InternalException, JSONException
	{
		this.pack(jsonWriter, object);
	}

	public HttpEntity pack() throws IOException, InternalException, JSONException
	{
		jsonWriter = new JsonWriter(new StringWriter());

		packData(obj, jsonWriter);

		StringEntity stringEntity = null;


		stringEntity = new StringEntity(jsonWriter.toString(), HTTP.UTF_8);


		return stringEntity;
	}

}
