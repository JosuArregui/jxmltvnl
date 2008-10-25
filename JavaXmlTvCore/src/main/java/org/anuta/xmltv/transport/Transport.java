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
import org.anuta.xmltv.exceptions.TransportException;

/**
 * Simple interface for transport.
 * 
 * @author afedorov
 */
public interface Transport {
    /**
     * Return text document for given url
     * 
     * @return text dicument
     * @throws TransportException
     */
    public String getText(String url) throws TransportException;
}
