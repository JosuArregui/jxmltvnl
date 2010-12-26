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
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class IMDBDaoHibernate extends HibernateDaoSupport implements IMDBDao  {
	public List getMovieRating(String title, String subtitle, int year) {
		Criteria criteria = getSession().createCriteria(MovieRating.class);
		if ((title!=null) && (title.length()>0)) criteria.add(Restrictions.eq("title", title).ignoreCase());
		if ((subtitle!=null) && (subtitle.length()>0)) criteria.add(Restrictions.eq("subtitle", subtitle).ignoreCase());
		if (year!=0) criteria.add(Restrictions.eq("year", new Integer(year)));
		return criteria.list();
		
	}
	public void save(MovieRating movieRating) {
		getHibernateTemplate().save(movieRating);
	}
	public void shutdown() {
		//getSessionFactory().getCurrentSession().createSQLQuery("SHUTDOWN").executeUpdate(); 
	}
	public void clean() {
	    getSessionFactory().getCurrentSession().createSQLQuery("DELETE FROM IMDB_MOVIE_RATING").executeUpdate();
	}
}
