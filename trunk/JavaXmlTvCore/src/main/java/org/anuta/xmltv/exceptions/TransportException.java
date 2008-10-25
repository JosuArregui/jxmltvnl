package org.anuta.xmltv.exceptions;

public class TransportException extends Exception {

    private static final long serialVersionUID = 2437642673421266441L;

    public TransportException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    public TransportException(String arg0) {
	super(arg0);
    }

    public TransportException(Throwable arg0) {
	super(arg0);
    }

    public TransportException() {
	super();
    }
}
