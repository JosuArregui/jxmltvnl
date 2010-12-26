package org.anuta.imdb;

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
import java.util.List;

import org.anuta.imdb.beans.MovieRating;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IMDBManagerImpl implements IMDBManager {
    private IMDBDao dao;
    

    private final static Log log = LogFactory.getLog(IMDBManagerImpl.class);

    public IMDBDao getDao() {
	return dao;
    }

    public void setDao(IMDBDao dao) {
	this.dao = dao;
    }

    public void saveMovieRating(MovieRating movieRating) {
	getDao().save(movieRating);
    }

    public void shutdown() {
	if (log.isDebugEnabled())
	    log.debug("Shutting down database");
	dao.shutdown();
    }

    public void clean() {
	if (log.isDebugEnabled())
	    log.debug("Cleaning the database");
	dao.clean();
    }

    public List getMovieRating(String title, String subtitle, int year) {
	return dao.getMovieRating(title, subtitle, year);
    }

    
}
