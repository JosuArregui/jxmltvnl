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

import java.io.IOException;
import java.util.List;

import org.anuta.imdb.beans.MovieRating;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IMDBAccess {
    private IMDBManager manager;
    private IMDBDownloader downloader;
    private IMDBRatingParser parser;
    private final static Log log = LogFactory.getLog(IMDBAccess.class);
    private final static String UNRATED = "unrated";

    public IMDBManager getManager() {
	return manager;
    }

    public void setManager(IMDBManager manager) {
	this.manager = manager;
    }

    public IMDBDownloader getDownloader() {
	return downloader;
    }

    public void setDownloader(IMDBDownloader downloader) {
	this.downloader = downloader;
    }

    public void startup() {

	try {
	    if (log.isDebugEnabled())
		log.debug("Starting up");
	    int result = getDownloader().download();

	    if (result == IMDBDownloader.RESULT_OK) {

		getManager().clean();
		long id = 0;
		
		getParser().init();
		MovieRating r;
		do {
		    r = getParser().getNextRating();
		    if (r != null) {
			r.setId(++id);
			getManager().saveMovieRating(r);
			if (id % 1000 == 0)
			    if (log.isInfoEnabled())
				log.info("Processed " + id + " ratings...");
		    }
		} while (r != null);

		getParser().destroy();
	    } else {
		if (log.isDebugEnabled())
		    log.debug("Result was not ok, no new ratings will be used");
	    }
	} catch (IOException e) {
	    if (log.isErrorEnabled())
		log.error("Error update imdb database", e);
	}

    }

    public void shutdown() {
	getManager().shutdown();
    }

    public MovieRating getRating(String title, String subtitle, int year) {
	if (log.isDebugEnabled())
	    log.debug("Looking for title/subtitle/year");
	List ratings = getManager().getMovieRating(title, subtitle, year);
	if (ratings.size() == 0) {
	    if (log.isDebugEnabled()) log.debug("Nothing found. Return "+UNRATED);
	    return null;
//	    if (log.isDebugEnabled())
//		log.debug("Looking for title/subtitle");
//	    ratings = getManager().getMovieRating(title, subtitle, 0);
//	    if (ratings.size() == 0) {
//		if (log.isDebugEnabled())
//		    log.debug("Looking for title");
//		ratings = getManager().getMovieRating(title, "", 0);
//		if (ratings.size() == 0) {
//		    if (log.isDebugEnabled()) log.debug("Nothing found. Return "+UNRATED);
//		    return UNRATED;
//		}    
//	    }
	}

	MovieRating mr = (MovieRating) ratings.get(0);
	if (log.isDebugEnabled())
	    log.debug("Using rating: " + mr);
	return mr;
    }

    public IMDBRatingParser getParser() {
        return parser;
    }

    public void setParser(IMDBRatingParser parser) {
        this.parser = parser;
    }
}
