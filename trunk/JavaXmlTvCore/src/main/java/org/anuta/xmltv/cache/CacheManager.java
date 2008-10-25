package org.anuta.xmltv.cache;

import java.util.Date;

import org.anuta.xmltv.exceptions.CacheException;
import org.anuta.xmltv.xmlbeans.Programme;

public interface CacheManager {
    public Programme getFromCache(Date date, String id) throws CacheException;

    public void saveInCache(Date date, String id, Programme program) throws CacheException;
}
