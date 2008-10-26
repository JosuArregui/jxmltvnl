package org.anuta.xmltv.beans;
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
import org.anuta.xmltv.grabber.EPGGrabber;

public class Channel {
    private String channelId;
    private EPGGrabber grabber;
    private String language = "nl";
    private String channelName = "";
  //  private String xmltvChannel

    public String getChannelName() {
	return channelName;
    }

    public void setChannelName(String channelName) {
	this.channelName = channelName;
    }

    public String getLanguage() {
	return language;
    }

    public void setLanguage(String language) {
	this.language = language;
    }

    public String getChannelId() {
	return channelId;
    }

    public void setChannelId(String channelId) {
	this.channelId = channelId;
    }

    public EPGGrabber getGrabber() {
	return grabber;
    }

    public void setGrabber(EPGGrabber grabber) {
	this.grabber = grabber;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer().append(getChannelId()).append(" - ").append(getChannelName());
	return sb.toString();
    }
    
    public String getXmltvChannelId() {
	return getGrabber().getMappedChannelId(getChannelId());
    }
}
