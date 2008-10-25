package org.anuta.xmltv.transport;
/*
 * Java xmltv grabber for tvgids.nl
 * Copyright (C) 2008 Alex Fedorov
 * fedor@anuta.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
import java.io.IOException;

import org.anuta.xmltv.exceptions.TransportException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTTPTransport implements Transport {
    private final static Log log = LogFactory.getLog(HTTPTransport.class);
    private String proxyAddress;
    private int proxyPort;
    private int retryCount = 3;
    private String encoding = "ISO8859_1";

    public int getRetryCount() {
	return retryCount;
    }

    public void setRetryCount(int retryCount) {
	this.retryCount = retryCount;
    }

    public String getProxyAddress() {
	return proxyAddress;
    }

    public void setProxyAddress(String proxyAddress) {
	this.proxyAddress = proxyAddress;
    }

    public int getProxyPort() {
	return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
	this.proxyPort = proxyPort;
    }

    public String getEncoding() {
	return encoding;
    }

    public void setEncoding(String encoding) {
	this.encoding = encoding;
    }

    public String getText(String url) throws TransportException {
	HttpClient client = new HttpClient();

	
	if (getProxyAddress() != null) {
	    if (log.isDebugEnabled())
		log.debug("Using proxy: " + getProxyAddress() + ":" + getProxyPort());
	    client.getHostConfiguration().setProxy(getProxyAddress(), getProxyPort());
	}
	GetMethod method = new GetMethod(url);
	if (getRetryCount() != 0) {
	    if (log.isDebugEnabled())
		log.debug("Set retry count to " + getRetryCount());
	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(getRetryCount(), false));
	}

	try {
	    // Execute the method.
	    int statusCode = client.executeMethod(method);

	    if (statusCode != HttpStatus.SC_OK)
		throw new TransportException("Server responce not ok, i've got " + statusCode);

	    byte[] responseBody = method.getResponseBody();
	    String ret = null;
	    if (getEncoding() != null)
		ret = new String(responseBody, getEncoding());
	    else
		ret = new String(responseBody);
	    if (log.isTraceEnabled())
		log.trace("Received document: " + ret);
	    return ret;
	} catch (HttpException e) {
	    throw new TransportException(e);
	} catch (IOException e) {
	    throw new TransportException(e);
	} finally {
	    method.releaseConnection();
	}
    }
}
