package org.anuta.xmltv.transport;

/*
 * Copyright (C) 2008 Alex Fedorov
 * Java xmltv grabber for tvgids.nl
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

import org.anuta.xmltv.exceptions.TransportException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTTPTransport implements Transport {
	private final static Log log = LogFactory.getLog(HTTPTransport.class);
	private String proxyAddress;
	private int proxyPort;
	private int retryCount = 3;
	private int socketTimeoutSeconds = 60;
	private String encoding = "ISO8859_1";

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setSocketTimeoutSeconds(int socketTimeoutSeconds) {
		this.socketTimeoutSeconds = socketTimeoutSeconds;
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

		if (log.isDebugEnabled()) {
			log.debug("Get text: " + url);
		}

		GetMethod method = createGetMethod(url);
		
		try {
			return executeWithRetry(method);
		} catch (HttpException e) {
			throw new TransportException(url, e);
		} catch (IOException e) {
			throw new TransportException(url, e);
		} finally {
			method.releaseConnection();
		}
	}
	
	private String executeWithRetry(GetMethod method) throws TransportException, HttpException, IOException {

		int tries = 0;
		while (tries <= getRetryCount()) {
			try {
 
				int statusCode = createHttpClient().executeMethod(method);

				if (statusCode != HttpStatus.SC_OK)
					throw new TransportException(
							"Server response not ok, i've got " + statusCode);

				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(
												method.getResponseBodyAsStream(), getEncoding()));

				String s;
				while ((s = br.readLine()) != null) {
					sb.append(s);
					if (s.toLowerCase().contains("</html>")) {
						log.debug("got complete html, no need to read or wait for whitespace...");
						break;
					}
				}
				return sb.toString();
			} catch (SocketTimeoutException e) {
				log.info("timed out: but retrying (" + tries + ")");
			}
			tries++;
		}
		throw new TransportException("Request timed out.");
	}
	
	private HttpClient createHttpClient() {
		HttpClient client = new HttpClient();
		if (StringUtils.isNotEmpty(getProxyAddress())) {
			client.getHostConfiguration().setProxy(getProxyAddress(),
					getProxyPort());
		}
		return client;
	}

	private GetMethod createGetMethod(String url) {
		GetMethod method = new GetMethod(url);
		if (getRetryCount() != 0) {
			if (log.isDebugEnabled())
				log.debug("Set retry count to " + getRetryCount());
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(getRetryCount(), false));
		}

		method.getParams().setIntParameter(
					HttpMethodParams.SO_TIMEOUT, this.socketTimeoutSeconds * 1000);
		return method;
	}

}
