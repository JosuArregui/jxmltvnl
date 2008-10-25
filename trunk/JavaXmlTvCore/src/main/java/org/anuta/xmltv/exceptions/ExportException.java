package org.anuta.xmltv.exceptions;

public class ExportException extends Exception {

    private static final long serialVersionUID = 4403057140735239733L;

    public ExportException() {
	super();
    }

    public ExportException(String message, Throwable cause) {
	super(message, cause);
    }

    public ExportException(String message) {
	super(message);
    }

    public ExportException(Throwable cause) {
	super(cause);
    }
}
