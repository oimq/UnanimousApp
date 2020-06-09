package com.unanimous;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;

public class FormEncodingBuilder {
    private static final MediaType CONTENT_TYPE =
            MediaType.parse("application/x-www-form-urlencoded");
    private final Buffer content = new Buffer();
    /** Add new key-value pair. */
    public FormEncodingBuilder add(String name, String value) {
        if (content.size() > 0) {
            content.writeByte('&');
        }
        try {
            content.writeUtf8(URLEncoder.encode(name, "UTF-8"));
            content.writeByte('=');
            content.writeUtf8(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        return this;
    }
    public RequestBody build() {
        if (content.size() == 0) {
            throw new IllegalStateException("Form encoded body must have at least one part.");
        }
        return RequestBody.create(CONTENT_TYPE, content.snapshot());
    }
}
