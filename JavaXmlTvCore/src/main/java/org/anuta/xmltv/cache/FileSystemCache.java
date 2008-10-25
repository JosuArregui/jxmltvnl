package org.anuta.xmltv.cache;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.anuta.xmltv.exceptions.CacheException;
import org.anuta.xmltv.xmlbeans.Programme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemCache implements CacheManager {
    private final static Log log = LogFactory.getLog(FileSystemCache.class);
    private String folder;

    public String getFolder() {
	return folder;
    }

    public void setFolder(String folder) {
	this.folder = folder;
    }

    public Programme getFromCache(Date date, String id) throws CacheException {
	File f = new File(folder);
	if (!f.exists() || (!f.isDirectory()))
	    return null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	String dirName = sdf.format(date);
	File dir = new File(f, dirName);
	if (!dir.exists())
	    return null;
	File file = new File(dir, id + ".xml");
	if (!file.exists())
	    return null;
	if (!file.canRead())
	    return null;
	Programme prog;
	try {
	    prog = Programme.Factory.parse(file);
	} catch (Exception e) {
	    if (log.isErrorEnabled())
		log.error("Error loading file " + id, e);
	    return null;
	}
	if (log.isDebugEnabled())
	    log.debug("Cache hit on " + id);
	return prog;
    }

    public void saveInCache(Date date, String id, Programme program) throws CacheException {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	File f = new File(folder);
	String dirName = sdf.format(date);
	File dir = new File(f, dirName);
	if (!dir.exists())
	    dir.mkdir();
	File file = new File(dir, id + ".xml");
	try {
	    program.save(file);
	    if (log.isDebugEnabled())
		log.debug("Cache store of " + id);
	} catch (IOException e) {
	    if (log.isErrorEnabled())
		log.error("Error saving file", e);
	    throw new CacheException("Error saving file");
	}
    }

}
