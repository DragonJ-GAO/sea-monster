package me.add1.network;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.util.List;

public class MultipartUtils {
	public static final String CONTENT_TYPE = "multipart/form-data; boundary=";
	public static final String PARAMETER_SEPARATOR = "\r\n--";

	public static String getContentType(final String boundary) {
		return String.format(CONTENT_TYPE + boundary);
	}

	public static boolean isEncoded(final HttpEntity entity) {
		final Header contentType = entity.getContentType();
		return (contentType != null && contentType.getValue().equalsIgnoreCase(CONTENT_TYPE));
	}

	public static String getFormDataContent(final List<? extends NameValuePair> parameters, final String boundary) throws IllegalArgumentException {

		if (parameters == null || parameters.size() == 0)
			throw new IllegalArgumentException("parameters error");
		final StringBuilder result = new StringBuilder();
		for (final NameValuePair parameter : parameters) {

			result.append(String.format("\r\n--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"\r\n\r\n%3$s", boundary, parameter.getName(),
                    parameter.getValue()));
		}
		return result.toString();
	}

	public static String getFileSeparator(final String boundary, String name, String fileName) {
		return String.format(
                "\r\n--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"; filename=\"%3$s\"\r\nContent-Type: application/octet-stream\r\n\r\n",
                boundary, name, fileName);
	}

	public static String getEndSeparator(final String boundary) {
		return PARAMETER_SEPARATOR + boundary;
	}

	public static String getFormDataContent(String contentType, List<? extends NameValuePair> parameters, String boundary) {
		if (parameters == null || parameters.size() == 0)
			throw new IllegalArgumentException("parameters error");
		final StringBuilder result = new StringBuilder();
		for (final NameValuePair parameter : parameters) {

			result.append(String.format("\r\n--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"\r\n\r\n%3$s\r\n\r\n%4$s", boundary,
                    parameter.getName(), parameter.getValue(), boundary));
		}
		return result.toString();
	}
}
