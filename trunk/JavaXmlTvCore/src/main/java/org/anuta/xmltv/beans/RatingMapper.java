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
import java.util.HashMap;
import java.util.Map;

public class RatingMapper {
    private Map mapping = new HashMap();
    private String system = "KW";

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public Map getMapping() {
        return mapping;
    }

    public void setMapping(Map mapping) {
        this.mapping = mapping;
    }
    
    public String mapRating(String txt) {
	if (txt==null) return "unknown";
	if (mapping.containsKey(txt)) return (String)mapping.get(txt); else return txt;
    }
}
