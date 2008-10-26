package org.anuta.xmltv;
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
import java.rmi.server.ExportException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.anuta.xmltv.beans.Channel;
import org.anuta.xmltv.beans.Program;
import org.anuta.xmltv.beans.ProgramComparator;
import org.anuta.xmltv.beans.Rating;
import org.anuta.xmltv.cache.CacheManager;
import org.anuta.xmltv.export.Export;
import org.anuta.xmltv.xmlbeans.Credits;
import org.anuta.xmltv.xmlbeans.Image;
import org.anuta.xmltv.xmlbeans.Lstring;
import org.anuta.xmltv.xmlbeans.Programme;
import org.anuta.xmltv.xmlbeans.Subtitles;
import org.anuta.xmltv.xmlbeans.TvDocument;
import org.anuta.xmltv.xmlbeans.TvDocument.Tv;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.Oustermiller.util.StringHelper;

public class XMLTVGrabber {
    private final static Log log = LogFactory.getLog(XMLTVGrabber.class);

    private List channels = new ArrayList();

    private CacheManager cache;

    private Export export;

    private long maxOverlap = 10; // 10 minutes

    private int overlapFixMode = 0; // 0 - middle, 1 - left, 2 - right

    private int daysToGrab = 4;

    public int getDaysToGrab() {
	return daysToGrab;
    }

    public void setDaysToGrab(int daysToGrab) {
	this.daysToGrab = daysToGrab;
    }

    public int getOverlapFixMode() {
	return overlapFixMode;
    }

    public void setOverlapFixMode(int overlapFixMode) {
	this.overlapFixMode = overlapFixMode;
    }

    public long getMaxOverlap() {
	return maxOverlap;
    }

    public void setMaxOverlap(long maxOverlap) {
	this.maxOverlap = maxOverlap;
    }

    public Export getExport() {
	return export;
    }

    public void setExport(Export export) {
	this.export = export;
    }

    public CacheManager getCache() {
	return cache;
    }

    public void setCache(CacheManager cache) {
	this.cache = cache;
    }

    public List getChannels() {
	return channels;
    }

    public void setChannels(List channels) {
	this.channels = channels;
    }

    public List getDelimitedValues(String value, String delimiter, boolean processAnd) {
	if (value == null)
	    return null;
	String values = value.trim();
	if (values.length() == 0)
	    return null;
	if (processAnd) {
	    values = values.replaceAll(" en ", delimiter);
	    if (values.endsWith("e.a"))
		values = values.substring(0, values.length() - 3);
	}
	StringTokenizer st = new StringTokenizer(values, delimiter);
	List ret = new ArrayList();
	while (st.hasMoreTokens()) {
	    String token = st.nextToken();
	    if (token != null) {
		token = token.trim();
		if (token.length() > 0)
		    ret.add(token);
	    }
	}
	if (ret.size() == 0)
	    return null;
	else
	    return ret;
    }

    public void grab() {
	int days = getDaysToGrab();
	SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyyMMddHHmmss ZZZZ"); // ZZZZ
	// SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd"); // ZZZZ
	TvDocument doc = TvDocument.Factory.newInstance();
	Tv tv = doc.addNewTv();
	tv.setGeneratorInfoName("anuta xmltv generator");
	tv.setGeneratorInfoUrl("http://www.anuta.org/xmltv");
	tv.setDate(sdfDateTime.format(new Date()));
	tv.setSourceDataUrl("http://www.tvgids.nl");

	int day = 0;
	while (day < days) {
	    log.debug("We are here");
	    Iterator it = getChannels().iterator();
	    while (it.hasNext()) {
		Channel channel = (Channel) it.next();
		if (log.isInfoEnabled())
		    log.info("Processing channel " + channel);
		List programs = channel.getGrabber().getPrograms(channel, new Date(), day);

		if (log.isDebugEnabled())
		    log.debug("Sorting programs");
		Collections.sort(programs, new ProgramComparator());

		adjustTiming(programs);

		if (day == 0) {
		    if (log.isDebugEnabled())
			log.debug("Add channel " + channel + " to xml");
		    org.anuta.xmltv.xmlbeans.Channel channelXml = tv.addNewChannel();
		    channelXml.setId(channel.getXmltvChannelId());
		    channelXml.setDisplayName(StringHelper.unescapeHTML(channel.getChannelName()));
		    if (channel.getChannelLogo()!=null) {
		      Image icon = channelXml.addNewIcon();
		      icon.setSrc(channel.getChannelLogo());
		    }
		}

		Iterator it2 = programs.iterator();
		while (it2.hasNext()) {
	
		    Program p = (Program) it2.next();
		    Programme prog = null;

		    // cache
		    if (getCache() != null) {
			try {
			    prog = getCache().getFromCache(p.getStartDate(), p.getId());
			} catch (Exception e) {
			    if (log.isWarnEnabled())
				log.warn("Cache error occured, reloading");
			}
		    }

		    if (prog == null) {
			Program p2 = channel.getGrabber().getProgram(p);

			if (log.isInfoEnabled())
			    log.info(p2);

			prog = tv.addNewProgramme();
			if ((p2.getDescription() != null) && (p2.getDescription().length() > 0)) {
			    Lstring desc = prog.addNewDesc();
			    desc.setLang(channel.getLanguage());
			    desc.setStringValue(StringHelper.unescapeHTML(p2.getDescription()));
			}

			prog.setChannel(channel.getXmltvChannelId());
			if (p2.getPremiere() != null) {
			    prog.setDate(p2.getPremiere());
			}

			// dates
			prog.setStart(sdfDateTime.format(p2.getStartDate()));
			prog.setStop(sdfDateTime.format(p2.getEndDate()));

			// title
			Lstring title = prog.addNewTitle();
			title.setLang(channel.getLanguage());
			title.setStringValue(StringHelper.unescapeHTML(p2.getTitle()));

			// ganre
			// TODO: Process description for movies
			if (p2.getGanre() != null) {
			    String ganre = channel.getGrabber().getMappedGanre(p2.getGanre());
			    Lstring cat = prog.addNewCategory();
			    cat.setLang(channel.getLanguage());
			    cat.setStringValue(StringHelper.unescapeHTML(ganre));
			}

			// credits
			if ((p2.getActors() != null) || (p2.getDirectors() != null) || (p2.getPresentors() != null)) {
			    if (log.isDebugEnabled())
				log.debug("Add credits");

			    Credits credits = prog.addNewCredits();
			    List list = getDelimitedValues(p2.getDirectors(), ",", true);
			    if (list != null) {
				Iterator dirit = list.iterator();
				while (dirit.hasNext()) {
				    String tmp = StringHelper.unescapeHTML((String) dirit.next());
				    credits.addDirector(tmp);
				}
			    }

			    list = getDelimitedValues(p2.getActors(), ",", true);
			    if (list != null) {
				Iterator dirit = list.iterator();
				while (dirit.hasNext()) {
				    String tmp = StringHelper.unescapeHTML((String) dirit.next());
				    credits.addActor(tmp);
				}
			    }

			    list = getDelimitedValues(p2.getPresentors(), ",", true);
			    if (list != null) {
				Iterator dirit = list.iterator();
				while (dirit.hasNext()) {
				    String tmp = StringHelper.unescapeHTML((String) dirit.next());
				    credits.addPresenter(tmp);
				}
			    }
			}

			// end of credits

			// prog.getAudio().setPresent("yes");
			// specials
			if (p2.getSpecials() != null) {
			    List list = getDelimitedValues(p2.getSpecials(), ",", false);
			    if (list != null) {
				Iterator dirit = list.iterator();
				while (dirit.hasNext()) {
				    String tmp = (String) dirit.next();
				    if (log.isDebugEnabled())
					log.debug("Process special: " + tmp);
				    if ("breedbeeld uitzending".equalsIgnoreCase(tmp)) {
					if (prog.getVideo() == null)
					    prog.addNewVideo();
					prog.getVideo().setAspect("16:9");
				    } else if ("teletekst ondertiteld".equalsIgnoreCase(tmp)) {
					Subtitles st = prog.addNewSubtitles();
					st.setType(Subtitles.Type.TELETEXT);
					Lstring lang = st.addNewLanguage();
					lang.setLang(channel.getLanguage());
					lang.setStringValue("Nederlands");
				    } else if ("stereo".equalsIgnoreCase(tmp)) {
					if (prog.getAudio() == null)
					    prog.addNewAudio();
					prog.getAudio().setStereo("stereo");
				    } else if ("dolby surround".equalsIgnoreCase(tmp)) {
					if (prog.getAudio() == null)
					    prog.addNewAudio();
					prog.getAudio().setStereo("surround");
				    }

				}
			    }
			} // end specials

			// rating
			if (p.getRating().size() > 0) {
			    Iterator dirit = p.getRating().iterator();
			    while (dirit.hasNext()) {
				Rating rat = (Rating) dirit.next();

				org.anuta.xmltv.xmlbeans.Rating rating = prog.addNewRating();
				rating.setSystem(rat.getSystem());
				rating.setValue(StringHelper.unescapeHTML(rat.getValue()));
				Image icon = rating.addNewIcon();
				icon.setSrc(rat.getIcon());
			    }
			} // end rating

			if (p.getSubTitle() != null) {
			    Lstring subtitle = prog.addNewSubTitle();
			    subtitle.setLang(channel.getLanguage());
			    subtitle.setStringValue(StringHelper.unescapeHTML(p.getSubTitle()));
			}

			// clumpIdx
			if (p.getClumpIdx() != null) {
			    prog.setClumpidx(p.getClumpIdx());
			}

			try {
			    if (getCache() != null)
				getCache().saveInCache(p2.getStartDate(), p2.getId(), prog);
			} catch (Exception e) {
			    if (log.isWarnEnabled())
				log.warn("Unable to save document to cache " + e);
			}
		    } else {
			tv.addNewProgramme().set(prog);
		    }
		}

	    }
	    day++;
	}

	try {
	    log.debug(doc.toString());
	    getExport().export(doc);
	} catch (ExportException e) {
	    if (log.isErrorEnabled())
		log.error("Unable to export document", e);
	}
    }

    private void adjustTiming(List programs) {
	if ((programs == null) || (programs.size() < 2))
	    return;

	for (int i = 0; i < (programs.size() - 1); i++) {
	    Program p1 = (Program) programs.get(i);
	    Program p2 = (Program) programs.get(i + 1);

	    if (p1.getEndDate().after(p2.getStartDate())) {
		if (log.isDebugEnabled())
		    log.debug("Overlap detected " + p1 + " " + p2);
		long overlap = p1.getEndDate().getTime() - p2.getStartDate().getTime();
		long overlapMinutes = Math.round(overlap / 60000);

		if (log.isDebugEnabled())
		    log.debug("Overlapped on " + overlapMinutes + " minutes");

		if (overlapMinutes <= getMaxOverlap()) {
		    if (log.isDebugEnabled())
			log.debug("Fix overlap with mode " + getOverlapFixMode());

		    if (0 == getOverlapFixMode()) {
			// middle
			overlap = overlap >> 1;
			p1.getEndDate().setTime(p1.getEndDate().getTime() - overlap);
			p2.setStartDate(p1.getEndDate());
		    } else if (2 == getOverlapFixMode()) {
			// p2 leading
			p1.setEndDate(p2.getStartDate());
		    } else if (1 == getOverlapFixMode()) {
			// p1 leading
			p2.setStartDate(p1.getEndDate());
		    } else
			throw new IllegalArgumentException("Overlap mode can be 0,1 or 2");
		} else {
		    if (log.isDebugEnabled())
			log.debug("Overlap is too long, lets mark it as special");
		    p1.setClumpIdx("0/2");
		    p2.setClumpIdx("1/2");

		}

	    }
	}
    }
}