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
