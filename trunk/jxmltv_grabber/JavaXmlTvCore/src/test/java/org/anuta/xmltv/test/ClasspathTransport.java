package org.anuta.xmltv.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.anuta.xmltv.exceptions.TransportException;
import org.anuta.xmltv.transport.Transport;

public class ClasspathTransport implements Transport {

	public String getText(String url) throws TransportException {
		System.out.println("ClasspathTransport.getText " + url);
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
		if (is == null) {
			System.out.println("Returning null.");
			return null;
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String s = null;
		try {
			while ((s = r.readLine()) != null) {
				buffer.append(s);
			}
			return buffer.toString();
		} catch (IOException e) {
			throw new TransportException(e);
		}
	}

}
