package org.anuta.xmltv.cache;
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
import java.util.Date;

import org.anuta.xmltv.exceptions.CacheException;
import org.anuta.xmltv.xmlbeans.Programme;

public interface CacheManager {
    public Programme getFromCache(Date date, String id) throws CacheException;

    public void saveInCache(Date date, String id, Programme program) throws CacheException;
}
