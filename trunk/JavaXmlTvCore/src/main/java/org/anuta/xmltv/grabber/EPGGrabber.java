package org.anuta.xmltv.grabber;

import java.util.Date;
import java.util.List;

import org.anuta.xmltv.beans.Channel;
import org.anuta.xmltv.beans.Program;

public interface EPGGrabber {
    public List getPrograms(Channel channel, Date date, int day);

    public Program getProgram(Program p);

    public String getMappedGanre(String ganre);

    public String getMappedRole(String role);
}
