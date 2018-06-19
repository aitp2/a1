package com.accenture.aitp.tailor.network;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

public class HttpPurge extends HttpRequestBase {

	public final static String METHOD_NAME = "PURGE";

    public HttpPurge() {
        super();
    }

    public HttpPurge(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpPurge(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
