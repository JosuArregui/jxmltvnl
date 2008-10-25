package org.anuta.xmltv.exceptions;

public class CacheException extends Exception {

    private static final long serialVersionUID = 4403057140735239733L;

    public CacheException() {
	super();
    }

    public CacheException(String message, Throwable cause) {
	super(message, cause);
    }

    public CacheException(String message) {
	super(message);
    }

    public CacheException(Throwable cause) {
	super(cause);
    }
}
