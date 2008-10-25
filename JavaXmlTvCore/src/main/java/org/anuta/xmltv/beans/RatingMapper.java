package org.anuta.xmltv.beans;

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
