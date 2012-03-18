package org.anuta.xmltv.grabber;
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
import java.util.List;

import org.anuta.xmltv.beans.Channel;
import org.anuta.xmltv.beans.Program;
import org.anuta.xmltv.exceptions.TransportException;

public interface EPGGrabber {

	public List<Program> getPrograms(Channel channel, Date date, int day) throws TransportException;

    public Program getProgram(Program p) throws TransportException;

    public String getMappedGanre(String ganre);

    public String getMappedRole(String role);
    
    public String getMappedChannelId(String channelId);
}
