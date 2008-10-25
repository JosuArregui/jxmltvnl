package org.anuta.xmltv.exceptions;
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
