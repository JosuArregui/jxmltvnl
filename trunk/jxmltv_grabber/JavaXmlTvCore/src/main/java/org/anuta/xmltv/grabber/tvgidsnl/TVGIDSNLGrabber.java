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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.anuta.xmltv.beans.Channel;
import org.anuta.xmltv.beans.Program;
import org.anuta.xmltv.beans.Rating;
import org.anuta.xmltv.beans.RatingMapper;
import org.anuta.xmltv.exceptions.TransportException;
import org.anuta.xmltv.grabber.EPGGrabber;
import org.anuta.xmltv.transport.HTTPTransport;
import org.anuta.xmltv.transport.Transport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import com.Oustermiller.util.StringHelper;

public class TVGIDSNLGrabber implements EPGGrabber {
    private static final String INFO_XSL = "/info.xsl";

    private static final String DESCRIPTION_XSL = "/description.xsl";
    
    private static final String TITLE_XSL = "/title.xsl";    

    private String xsltPath = "src/main/xslt";

    private String tvgidsurl = "http://www.tvgids.nl";

    private String noData = "Over dit programma zijn geen detailgegevens bekend.";

    private static final Log log = LogFactory.getLog(TVGIDSNLGrabber.class);

    private Map ganreMapping = new HashMap();

    private RatingMapper ratingMapper;

    private Transport transport;

    private String longDateFormat = "EEEEEEEEEEEE dd MMMM yyyy HH:mm";

    private Map roleMapping = new HashMap();

    private String xmltvSuffix = ""; // additional to the channel id
    // (.tvgids.nl)

    private long TWALF_HOURS = 1000 * 60 * 60 * 12;

    public final String getMappedChannelId(final String channelId) {
        StringBuffer sb = new StringBuffer();
        sb.append(channelId);
        sb.append(getXmltvSuffix());
        return sb.toString();
    }

    public final String getXmltvSuffix() {
        return xmltvSuffix;
    }

    public final void setXmltvSuffix(final String xmltvSuffix) {
        this.xmltvSuffix = xmltvSuffix;
    }

    public final String getLongDateFormat() {
        return longDateFormat;
    }

    public final void setLongDateFormat(final String longDateFormat) {
        this.longDateFormat = longDateFormat;
    }

    public final Transport getTransport() {
        return transport;
    }

    public final void setTransport(final Transport transport) {
        this.transport = transport;
    }

    public final String getMappedGanre(final String ganre) {
        if (getGanreMapping().containsKey(ganre.toLowerCase())) {
            return (String) getGanreMapping().get(ganre.toLowerCase());
        } else {
            return ganre;
        }
    }

    public final String getMappedRole(final String role) {
        if (getRoleMapping().containsKey(role.toLowerCase())) {
            return (String) getRoleMapping().get(role.toLowerCase());
        } else {
            return role;
        }
    }

    public final Map getRoleMapping() {
        return roleMapping;
    }

    public final void setRoleMapping(final Map roleMapping) {
        this.roleMapping = roleMapping;
    }

    public final Map getGanreMapping() {
        return ganreMapping;
    }

    public final void setGanreMapping(final Map ganreMapping) {
        this.ganreMapping = ganreMapping;
    }

    /**
     * Converts date from dutch format to date object.
     * 
     * @param date mandag 12 october 2008
     * @param time 12:02
     * @param nextDay the day will be incremented if boolean
     * @return parsed date
     */
    private Date convertDate(final String date, final String time, final boolean nextDay) throws ParseException {
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

    private String formatDescription(final String description) {
        if (noData.equals(description)) {
            return "";
        } else {
            return description.trim();
        }
    }

    public final List getPrograms(final Channel channel, final Date date, final int day) {
        Date lastStartDate = null;
        Date lastEndDate = null;
        boolean startDateNextDay = false;
        boolean endDateNextDay = false;
        List<Program> programs = new ArrayList<Program>();

        String pageUrl = getTvgidsurl() + "/zoeken/?q=&d=" + day + "&z=" + channel.getChannelId() + "&t=&g=&v=0";

        try {

            if (log.isDebugEnabled()) {
                log.debug("Fetch " + pageUrl);
            }

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
                            if ("time".equalsIgnoreCase(cl)) {
                                time = spans[j].getText().toString();
                            } else if ("title".equalsIgnoreCase(cl)) {
                                title = spans[j].getText().toString();
                            } else if ("channel".equalsIgnoreCase(cl)) {
                                chan = spans[j].getText().toString();
                            }
                        }

                        if (time.length() == 0) {
                            if (log.isWarnEnabled()) {
                                log.warn("Unusable program. No time defined.");
                            }
                            continue;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug(time);
                            log.debug(title);
                            log.debug(chan);
                        }
                        
                        if (title.endsWith("...")) {
                        	if (log.isDebugEnabled()) {
                        		log.debug("Found trimmed title: " + title);
                        	}
                        }

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
                            if (log.isWarnEnabled()) {
                                log.warn("Unusable program times. Empty.");
                            }
                            continue;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("StartTime " + dateText + " " + startTime);
                        }
                        try {
                            Date sd = convertDate(dateText, startTime, startDateNextDay);
                            if (lastStartDate == null) {
                                lastStartDate = sd;
                            } else {
                                if (((lastStartDate.getTime() - sd.getTime()) > TWALF_HOURS) && (!startDateNextDay)) {
                                    startDateNextDay = true;
                                    sd = convertDate(dateText, startTime, startDateNextDay);
                                }
                                lastStartDate = sd;
                            }
                            if (log.isDebugEnabled()) {
                              log.debug("START: " + sd);
                            }
                                                       
                            p.setStartDate(sd);
 
                        } catch (ParseException e) {
                            if (log.isErrorEnabled()) {
                                log.error("Unable to parse date");
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("EndTime " + dateText + " " + endTime);
                        }
                        try {
                            Date ed = convertDate(dateText, endTime, endDateNextDay);
                            if (lastEndDate == null) {
                                lastEndDate = ed;
                            } else {
                                if (((lastEndDate.getTime() - ed.getTime()) > TWALF_HOURS) && (!endDateNextDay)) {
                                    endDateNextDay = true;
                                    ed = convertDate(dateText, endTime, endDateNextDay);
                                }
                                lastEndDate = ed;
                            }
                            if (log.isDebugEnabled()) {
                            	log.debug("END  : " + ed);
                            }
                            p.setEndDate(ed);
                        } catch (ParseException e) {
                            if (log.isErrorEnabled()) {
                                log.error("Unable to parse date ");
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Times: " + p.getStartDate() + " " + p.getEndDate());
                        }
                        p.setChannelId(channel.getChannelId());

                        if (p.getStartDate() == null) {
                            if (programs.size() > 0) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Start time is unknown, getting one from previous program");
                                }
                                Date prevEnd = ((Program) programs.get(programs.size() - 1)).getEndDate();
                                if (prevEnd == null) {
                                    if (log.isWarnEnabled()) {
                                        log.warn("Unusable program. No start time.");
                                    }
                                    continue;
                                }
                                p.setStartDate(prevEnd);
                            } else {
                                if (log.isWarnEnabled()) {
                                    log.warn("Unusable program. No start time.");
                                }
                                continue;
                            }
                        } else {
                            if (programs.size() > 0) {
                                // check prev program end time
                                Program prevProgram = (Program) programs.get(programs.size() - 1);
                                Date prevEnd = prevProgram.getEndDate();
                                if (prevEnd == null) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Fixing previous program date");
                                    }
                                    prevProgram.setEndDate(p.getStartDate());
                                }
                            }
                        }

                        if ((p.getEndDate() == null) && (i == (programNodes.length - 1))) {
                            if (log.isWarnEnabled()) {
                                log.warn("Unusable program. No end time for last program");
                            }
                            continue;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Add programm to the guide " + p.getLongTitle());
                        }
                        programs.add(p);
                    }

                }
            }

        } catch (TransportException e) {
            if (log.isErrorEnabled()) {
                log.error("Transport exception occured", e);
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("IO exception occured", e);
            }
        }

        // fix program times
        if (programs.size() > 1) {
            for (int i = 0; i < programs.size() - 1; i++) {
                programs.get(i).setEndDate(programs.get(i + 1).getStartDate());
            }
        }
        return programs;
    }

    /**
     * Extract text from xml.
     * 
     * @param xml
     * @param xslTemplate
     * @return extracted text
     */
    private String extractText(final String xml, final String xslTemplate) {
            try {
                log.debug("XML: " + xml);
                javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();

                javax.xml.transform.Source stylesheet = new StreamSource(new FileInputStream(getXsltPath() + xslTemplate));

                Transformer transformer = tFactory.newTransformer(stylesheet);

                StringWriter sw = new StringWriter();
                transformer.transform(new javax.xml.transform.stream.StreamSource(new ByteArrayInputStream(xml
                    .getBytes("UTF-8"))), new StreamResult(sw));
                String ret = sw.toString().trim();
                log.debug("RET: " + ret);
                ret = StringHelper.unescapeHTML(ret);
                log.debug("RETUS: " + ret);
                return ret;
            } catch (FileNotFoundException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error in xslt transformation", e);
                }
                return null;

            } catch (TransformerConfigurationException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error in xslt transformation", e);
                }
                return null;

            } catch (UnsupportedEncodingException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error in xslt transformation", e);
                }
                return null;

            } catch (TransformerFactoryConfigurationError e) {
                if (log.isErrorEnabled()) {
                    log.error("Error in xslt transformation", e);
                }
                return null;

            } catch (TransformerException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error in xslt transformation", e);
                }
                return null;

            }
    }

//    private String extractText(final TagNode tn, final String xslTemplate) {
//        try {
//            HtmlCleaner cleaner = new HtmlCleaner();
//            CleanerProperties props = cleaner.getProperties();
//            props.setOmitComments(true);
//            props.setOmitUnknownTags(true);
//            props.setTranslateSpecialEntities(true);
//            props.setAllowHtmlInsideAttributes(false);
//
//            DomSerializer myDom = new DomSerializer(props);
//            Document doc = myDom.createDOM(tn);
//
//            TransformerFactory tranFactory = TransformerFactory.newInstance();
//            Transformer aTransformer = tranFactory.newTransformer();
//            Source src = new DOMSource(doc);
//            Writer outWriter = new StringWriter();
//            Result dest = new StreamResult(outWriter);
//            aTransformer.transform(src, dest);
//
//            String xml = outWriter.toString();
//            xml = xml.replace("&#13;", "");
//            xml = xml.replace("&#10;", "");
//            xml = xml.replace('\n', ' ');
//            xml = xml.replace('\r', ' ');
//            xml = xml.replace('\t', ' ');
//
//            // xml = StringHelper.unescapeHTML(xml);
//
//            // log.debug(xml);
//
//            return extractText(xml, xslTemplate);
//
//        } catch (Exception e) {
//            if (log.isErrorEnabled()) {
//                log.error("Error in xslt transformation", e);
//            }
//            return null;
//        }
//    }

    /**
     * Convert tag node to xml.
     * 
     * @param tn
     * @return
     */
    private String getXml(final TagNode tn) {
        try {
            HtmlCleaner cleaner = new HtmlCleaner();
            CleanerProperties props = cleaner.getProperties();
            props.setOmitComments(true);
            props.setOmitUnknownTags(true);
            props.setTranslateSpecialEntities(true);
            props.setAllowHtmlInsideAttributes(false);
            // props.setAdvancedXmlEscape(true);
            // props.setTranslateSpecialEntities(true);

            // DomSerializer myDom = new DomSerializer(props);
            // Document doc = myDom.createDOM(tn);

            DomSerializer myDom = new DomSerializer(props, false); // do not
            // escape
            // anything
            Document doc = myDom.createDOM(tn);

            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();
            Source src = new DOMSource(doc);
            Writer outWriter = new StringWriter();
            Result dest = new StreamResult(outWriter);
            aTransformer.transform(src, dest);

            String xml = outWriter.toString();
            xml = xml.replace("&#13;", "");
            xml = xml.replace("&#10;", "");
            xml = xml.replace('\n', ' ');
            xml = xml.replace('\r', ' ');
            xml = xml.replace('\t', ' ');
            return xml;

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error in xml serialization", e);
            }
            return null;
        }
    }

    /**
     * Get extended program data
     * 
     * @param p not fully loaded program
     * @return fully loaded program if possible
     */
    public final Program getProgram(final Program p) {
        try {
        	
            String html = getTransport().getText(p.getUrl());
            HtmlCleaner cleaner = new HtmlCleaner();

            // take default cleaner properties
            CleanerProperties props = cleaner.getProperties();
            props.setOmitComments(true);
            props.setOmitUnknownTags(true);
            props.setTranslateSpecialEntities(true);
            props.setAllowHtmlInsideAttributes(false);

            TagNode node = cleaner.clean(html);
            TagNode body = node.findElementByName("body", true);

            String xml = getXml(body);

            String description = extractText(xml, DESCRIPTION_XSL);
            String longTitle = extractText(xml, TITLE_XSL);
            
            p.setDescription(description);
            if ((longTitle == null) || (longTitle.trim().length() == 0)) {
            	p.setLongTitle(p.getTitle());
            } else {
            	p.setLongTitle(longTitle.trim());
            }
            
            String info = extractText(xml, INFO_XSL);
            if (info != null) {
                if (log.isDebugEnabled()) {
                	log.debug(info);
                }
                StringTokenizer st = new StringTokenizer(info, "|^|");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token == null) {
                        continue;
                    } else {
                        token = token.trim();
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Found token: " + token);
                    }

                    int pos = token.indexOf(':');
                    if (pos == -1) {
                        continue;
                    }
                    String key = token.substring(0, pos).trim().toLowerCase();
                    String value = token.substring(pos + 1, token.length()).trim();
                    if (key.length() == 0) {
                        continue;
                    }

                    if ("genre".equalsIgnoreCase(key)) {
                        if (value.length() == 0) {
                            value = "Overige";
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Found genre: " + value);
                        }
                        p.setGanre(value);
                    } else if ("acteurs".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found acteurs: " + value);
                        }
                        p.setActors(value);
                    } else if ("jaar van premiere".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found premiere: " + value);
                        }
                        p.setPremiere(value);
                    } else if ("titel aflevering".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found sub title: " + value);
                        }
                        p.setSubTitle(value);
                    } else if ("presentatie".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found presentors: " + value);
                        }
                        p.setPresentors(value);
                    } else if ("bijzonderheden".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found specials: " + value);
                        }
                        p.setSpecials(value);
                    } else if ("regisseur".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found director: " + value);
                        }
                        p.setDirectors(value);
                    } else if ("kijkwijzer".equals(key)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found kijkwijzer picture: " + value);
                        }
                        String src = "";
                        String text = "";

                        int spos = value.indexOf(" ");
                        if (spos == -1) {
                            src = value;
                        } else {
                            src = value.substring(0, spos).trim();
                            text = value.substring(spos + 1, value.length()).trim();

                            if (getRatingMapper() != null) {
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

                            }
                        }
                    }
                }
            }
            p.setFullyLoaded(true);
        } catch (TransportException e) {
            if (log.isErrorEnabled()) {
                log.error("Transport exception occured", e);
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("IO exception occured", e);
            }
        } finally {
        }
        return p;
    }

    public final String getNoData() {
        return noData;
    }

    public final void setNoData(final String noData) {
        this.noData = noData;
    }

    public final String getTvgidsurl() {
        return tvgidsurl;
    }

    public final void setTvgidsurl(final String tvgidsurl) {
        this.tvgidsurl = tvgidsurl;
    }

    public final RatingMapper getRatingMapper() {
        return ratingMapper;
    }

    public final void setRatingMapper(final RatingMapper ratingMapper) {
        this.ratingMapper = ratingMapper;
    }

    public static void main(final String[] args) {
        TVGIDSNLGrabber grabber = new TVGIDSNLGrabber();
        HTTPTransport transport = new HTTPTransport();
        transport.setEncoding("ISO8859_1");
        grabber.setTransport(transport);
        grabber.setGanreMapping(new HashMap());
        grabber.setNoData("NODATA");
        grabber.setRatingMapper(new RatingMapper());
        grabber.setRoleMapping(new HashMap());

        Program p = new Program();
        p.setUrl(grabber.getTvgidsurl() + "/programma/7939782/Advocaat_van_de_duivel/");
        grabber.getProgram(p);

    }

    public final String getXsltPath() {
        return xsltPath;
    }

    public final void setXsltPath(final String xsltPath) {
        this.xsltPath = xsltPath;
    }

}
