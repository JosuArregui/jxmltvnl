package org.anuta.imdb.beans;

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
import java.math.BigDecimal;

public class MovieRating {
    private long id;
    private String title;
    private String subtitle;
    private int year;
    private BigDecimal rating;

    public String getSubtitle() {
	return subtitle;
    }

    public void setSubtitle(String subtitle) {
	this.subtitle = subtitle;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public int getYear() {
	return year;
    }

    public void setYear(int year) {
	this.year = year;
    }

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public BigDecimal getRating() {
	return rating;
    }

    public void setRating(BigDecimal rating) {
	this.rating = rating;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(getId()).append("|");
	sb.append(getTitle()).append("|");
	sb.append(getSubtitle()).append("|");
	sb.append(getYear()).append("|");
	sb.append(getRating());
	return sb.toString();
    }

    /**
     * Returns formatted rating/10 string
     * 
     * @return
     */
    public String getFormattedRating() {
	int value = Math.round(getRating().toBigInteger().floatValue());
	if (value > 10)
	    value = 10;
	return value + "/" + 10;
    }
}
