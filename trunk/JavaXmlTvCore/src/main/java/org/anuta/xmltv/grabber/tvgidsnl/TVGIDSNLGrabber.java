package org.anuta.xmltv.grabber.tvgidsnl;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.anuta.xmltv.beans.Channel;
import org.anuta.xmltv.beans.Program;
import org.anuta.xmltv.beans.Rating;
import org.anuta.xmltv.beans.RatingMapper;
import org.anuta.xmltv.exceptions.TransportException;
import org.anuta.xmltv.grabber.EPGGrabber;
import org.anuta.xmltv.transport.Transport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class TVGIDSNLGrabber implements EPGGrabber {
    private String tvgidsurl = "http://www.tvgids.nl";

    private String noData = "Over dit programma zijn geen detailgegevens bekend.";

    private final static Log log = LogFactory.getLog(TVGIDSNLGrabber.class);

    private Map ganreMapping = new HashMap();

    private RatingMapper ratingMapper;

    private Transport transport;
    
    private String longDateFormat = "EEEEEEEEEEEE dd MMMM yyyy HH:mm";
    
    private Map roleMapping = new HashMap();

    public String getLongDateFormat() {
        return longDateFormat;
    }

    public void setLongDateFormat(String longDateFormat) {
        this.longDateFormat = longDateFormat;
    }

    public Transport getTransport() {
	return transport;
    }

    public void setTransport(Transport transport) {
	this.transport = transport;
    }

    public String getMappedGanre(String ganre) {
	if (getGanreMapping().containsKey(ganre.toLowerCase()))
	    return (String) getGanreMapping().get(ganre.toLowerCase());
	else
	    return ganre;
    }

    public String getMappedRole(String role) {
	if (getRoleMapping().containsKey(role.toLowerCase()))
	    return (String) getRoleMapping().get(role.toLowerCase());
	else
	    return role;
    }

   
    public Map getRoleMapping() {
	return roleMapping;
    }

    public void setRoleMapping(Map roleMapping) {
	this.roleMapping = roleMapping;
    }

    public Map getGanreMapping() {
	return ganreMapping;
    }

    public void setGanreMapping(Map ganreMapping) {
	this.ganreMapping = ganreMapping;
    }

    /**
     * Converts date from dutch format to date object
     * 
     * @param date
     *                mandag 12 october 2008
     * @param time
     *                12:02
     * @param nextDaythe
     *                day will be incremented if boolean
     * @return parsed date
     */
    private Date convertDate(String date, String time, boolean nextDay) throws ParseException {
	Date td = null;
	SimpleDateFormat sdf = new SimpleDateFormat(getLongDateFormat(), new Locale("NL", "nl"));
	StringBuffer sb = new StringBuffer().append(date).append(" ").append(time);
	td = sdf.parse(sb.toString());
	if (nextDay) {
	    Calendar c = Calendar.getInstance();
	    c.setTime(td);
	    c.add(Calendar.DAY_OF_YEAR, 1);
	    td = c.getTime();
	}
	return td;
    }

    private String formatDescription(String description) {
	if (noData.equals(description))
	    return "";
	else
	    return description.trim();
    }

    public List getPrograms(Channel channel, Date date, int day) {
	Date lastStartDate = null;
	Date lastEndDate = null;
	boolean startDateNextDay = false;
	boolean endDateNextDay = false;
	List programs = new ArrayList();

	String pageUrl = getTvgidsurl() + "/zoeken/?q=&d=" + day + "&z=" + channel.getChannelId() + "&t=&g=&v=0";

	try {
	    String html = getTransport().getText(pageUrl);
	    // Execute the method.
	    HtmlCleaner cleaner = new HtmlCleaner();

	    // take default cleaner properties
	    CleanerProperties props = cleaner.getProperties();
	    props.setOmitComments(true);
	    props.setOmitUnknownTags(true);
	    props.setAllowHtmlInsideAttributes(false);

	    TagNode node = cleaner.clean(html);

	    TagNode[] titleNodes = node.getElementsByAttValue("class", "programs", true, true);
	    String dateText = "";
	    if (titleNodes.length == 1) {
		dateText = titleNodes[0].getElementsByName("h2", true)[0].getText().toString();
		log.debug("Date " + dateText);
	    }

	    // get all programs
	    TagNode[] programNodes = node.getElementsByAttValue("class", "program", true, true);
	    for (int i = 0; i < programNodes.length; i++) {
		TagNode progNode = programNodes[i];

		TagNode[] atag = progNode.getElementsByName("a", true);
		if (atag.length > 0) {
		    String url = atag[0].getAttributeByName("href");
		    log.debug(url);

		    TagNode[] spans = atag[0].getElementsByName("span", true);
		    if (spans.length > 0) {
			String time = "";
			String title = "";
			String chan = "";
			for (int j = 0; j < spans.length; j++) {
			    String cl = spans[j].getAttributeByName("class");
			    if ("time".equalsIgnoreCase(cl))
				time = spans[j].getText().toString();
			    else if ("title".equalsIgnoreCase(cl))
				title = spans[j].getText().toString();
			    else if ("channel".equalsIgnoreCase(cl))
				chan = spans[j].getText().toString();
			}

			if (time.length() == 0) {
			    if (log.isWarnEnabled())
				log.warn("Unusable program. No time defined.");
			    continue;
			}

			if (log.isDebugEnabled())
			    log.debug(time);
			if (log.isDebugEnabled())
			    log.debug(title);
			if (log.isDebugEnabled())
			    log.debug(chan);

			Program p = new Program();

			p.setUrl(getTvgidsurl() + url);
			p.setFullyLoaded(false);
			p.setTitle(title);

			// time processing
			String startTime = "";
			String endTime = "";
			int minIndex = time.indexOf("-");
			if (minIndex >= 0) {
			    startTime = time.substring(0, minIndex).trim();
			    endTime = time.substring(minIndex + 1).trim();
			} else {
			    if (log.isWarnEnabled())
				log.warn("Unusable program times. Empty.");
			    continue;
			}

			if (log.isDebugEnabled())
			    log.debug("StartTime " + dateText + " " + startTime);
			try {
			    Date sd = convertDate(dateText, startTime, startDateNextDay);
			    if (lastStartDate == null)
				lastStartDate = sd;
			    else {
				if (lastStartDate.after(sd) && (!startDateNextDay)) {
				    startDateNextDay = true;
				    sd = convertDate(dateText, startTime, startDateNextDay);
				}
				lastStartDate = sd;
			    }
			    log.debug("START: " + sd);
			    p.setStartDate(sd);
			} catch (Exception e) {
			    if (log.isErrorEnabled())
				log.error("Unable to parse date");
			}

			if (log.isDebugEnabled())
			    log.debug("EndTime " + dateText + " " + endTime);
			try {
			    Date ed = convertDate(dateText, endTime, endDateNextDay);
			    if (lastEndDate == null)
				lastEndDate = ed;
			    else {
				if (lastEndDate.after(ed) && (!endDateNextDay)) {
				    endDateNextDay = true;
				    ed = convertDate(dateText, endTime, endDateNextDay);
				}
				lastEndDate = ed;
			    }
			    log.debug("END  : " + ed);
			    p.setEndDate(ed);
			} catch (Exception e) {
			    if (log.isErrorEnabled())
				log.error("Unable to parse date ");
			}

			if (log.isDebugEnabled())
			    log.debug("Times: " + p.getStartDate() + " " + p.getEndDate());
			p.setChannelId(channel.getChannelId());

			if (p.getStartDate() == null) {
			    if (programs.size() > 0) {
				if (log.isDebugEnabled())
				    log.debug("Start time is unknown, getting one from previous program");
				Date prevEnd = ((Program) programs.get(programs.size() - 1)).getEndDate();
				if (prevEnd == null) {
				    if (log.isWarnEnabled())
					log.warn("Unusable program. No start time.");
				    continue;
				}
				p.setStartDate(prevEnd);
			    } else {
				if (log.isWarnEnabled())
				    log.warn("Unusable program. No start time.");
				continue;
			    }
			} else {
			    if (programs.size() > 0) {
				// check prev program end time
				Program prevProgram = (Program) programs.get(programs.size() - 1);
				Date prevEnd = prevProgram.getEndDate();
				if (prevEnd == null) {
				    if (log.isDebugEnabled())
					log.debug("Fixing previous program date");
				    prevProgram.setEndDate(p.getStartDate());
				}
			    }
			}

			if ((p.getEndDate() == null) && (i == (programNodes.length - 1))) {
			    if (log.isWarnEnabled())
				log.warn("Unusable program. No end time for last program");
			    continue;
			}

			if (log.isDebugEnabled())
			    log.debug("Add programm to the guide " + p.getTitle());
			programs.add(p);
		    }

		}
	    }

	} catch (TransportException e) {
	    if (log.isErrorEnabled())
		log.error("Transport exception occured", e);
	} catch (IOException e) {
	    if (log.isErrorEnabled())
		log.error("IO exception occured", e);
	}
	return programs;
    }

    /**
     * Get extended program data
     * 
     * @param p
     *                not fully loaded program
     * @return fully loaded program if possible
     */
    public Program getProgram(Program p) {
	try {
	    String html = getTransport().getText(p.getUrl());
	    HtmlCleaner cleaner = new HtmlCleaner();

	    // take default cleaner properties
	    CleanerProperties props = cleaner.getProperties();
	    props.setOmitComments(true);
	    props.setOmitUnknownTags(true);
	    props.setAllowHtmlInsideAttributes(false);

	    TagNode node = cleaner.clean(html);

	    TagNode detailBox = node.findElementByAttValue("class", "detailBox", true, true);
	    if (detailBox != null) {
		List details = detailBox.getElementListByName("div", true);
		if (details != null) {
		    Iterator it = details.iterator();
		    while (it.hasNext()) {
			TagNode tn = (TagNode) it.next();
			String cl = tn.getAttributeByName("class");
			log.debug("Found " + cl);

			if ("description".equalsIgnoreCase(cl)) {
			    // process description
			    List descr = tn.getElementListByName("span", true);
			    if (descr != null) {
				Iterator it2 = descr.iterator();
				while (it2.hasNext()) {
				    TagNode tn2 = (TagNode) it2.next();
				    String cl2 = tn2.getAttributeByName("class");
				    if ("text".equalsIgnoreCase(cl2)) {
					List p3 = tn2.getElementListByName("p", true);
					if ((p3 != null) && (p3.size() > 0)) {
					    p.setDescription(formatDescription(((TagNode) p3.get(0)).getText().toString()));
					}
				    } else if ("type".equalsIgnoreCase(cl2)) {
					p.setType(tn2.getText().toString());

				    }
				}
			    }
			} else if ("info".equalsIgnoreCase(cl)) {
			    List p4 = tn.getElementListByName("li", true);
			    if (p4 != null) {
				Iterator it4 = p4.iterator();
				while (it4.hasNext()) {
				    TagNode tn4 = (TagNode) it4.next();

				    // check for images
				    if (getRatingMapper() != null) {
					List images = tn4.getElementListByName("img", true);
					if ((images != null) && (images.size() > 0)) {
					    if (log.isDebugEnabled())
						log.debug("Found images");

					    Iterator it5 = images.iterator();
					    while (it5.hasNext()) {
						TagNode tn5 = (TagNode) it5.next();

						String src = tn5.getAttributeByName("src");
						String text = tn5.getAttributeByName("alt");
						if ((src != null) && (text != null)) {
						    Rating rat = new Rating();
						    rat.setSystem(getRatingMapper().getSystem());
						    int ls = src.lastIndexOf('/');
						    if ((ls != -1) && (ls < src.length())) {
							src = src.substring(ls + 1);
							rat.setIcon(src);
							rat.setValue(getRatingMapper().mapRating(text));
							p.getRating().add(rat);
						    }
						}

						if (log.isDebugEnabled())
						    log.debug("Found: " + src + " " + text);
					    }

					    continue;
					}
				    }

				    String s = tn4.getText().toString();
				    s = s.replace('\r', ' ');
				    s = s.replace('\n', ' ');
				    s = s.trim();
				    int cp = s.indexOf(':');
				    if (cp > -1) {
					String key = s.substring(0, cp).trim();
					String value = s.substring(cp + 1).trim();
					if (log.isTraceEnabled())
					    log.trace(key + " = " + value);

					if ("genre".equalsIgnoreCase(key)) {
					    if (value.length() == 0)
						value = "Overige";
					    if (log.isDebugEnabled())
						log.debug("Found genre: " + value);
					    p.setGanre(value);
					} else if ("acteurs".equalsIgnoreCase(key)) {
					    if (log.isDebugEnabled())
						log.debug("Found acteurs: " + value);
					    p.setActors(value);
					} else if ("jaar van premiere".equalsIgnoreCase(key)) {
					    if (log.isDebugEnabled())
						log.debug("Found premiere: " + value);
					    p.setPremiere(value);
					} else if ("titel aflevering".equalsIgnoreCase(key)) {
					    if (log.isDebugEnabled())
						log.debug("Found sub title: " + value);
					    p.setSubTitle(value);
					} else if ("presentatie".equalsIgnoreCase(key)) {
					    if (log.isDebugEnabled())
						log.debug("Found presentors: " + value);
					    p.setPresentors(value);
					} else if ("bijzonderheden".equalsIgnoreCase(key)) {
					    if (log.isDebugEnabled())
						log.debug("Found specials: " + value);
					    p.setSpecials(value);
					} else if ("regisseur".equalsIgnoreCase(key)) {
					    if (log.isDebugEnabled())
						log.debug("Found director: " + value);
					    p.setDirectors(value);
					}

				    }
				}
			    }
			}
		    }
		}
	    }
	    p.setFullyLoaded(true);
	} catch (TransportException e) {
	    if (log.isErrorEnabled())
		log.error("Transport exception occured", e);
	} catch (IOException e) {
	    if (log.isErrorEnabled())
		log.error("IO exception occured", e);
	} finally {
	}
	return p;
    }

    public String getNoData() {
	return noData;
    }

    public void setNoData(String noData) {
	this.noData = noData;
    }

    public String getTvgidsurl() {
	return tvgidsurl;
    }

    public void setTvgidsurl(String tvgidsurl) {
	this.tvgidsurl = tvgidsurl;
    }

    public RatingMapper getRatingMapper() {
	return ratingMapper;
    }

    public void setRatingMapper(RatingMapper ratingMapper) {
	this.ratingMapper = ratingMapper;
    }

}
