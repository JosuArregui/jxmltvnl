package org.anuta.xmltv.cache;

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
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.anuta.xmltv.exceptions.CacheException;
import org.anuta.xmltv.xmlbeans.Programme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemCache implements CacheManager {
	private final static Log log = LogFactory.getLog(FileSystemCache.class);
	private String folder;
	private static final DateFormat CACHE_FOLDER_FORMAT = new SimpleDateFormat("yyyyMMdd");

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public Programme getFromCache(Date date, String id) throws CacheException {
		File cacheFile = buildCacheFile(buildCachePath(date), id);
		if (!cacheFile.exists() || !cacheFile.canRead())
			return null;

		if (log.isDebugEnabled())
			log.debug("Cache hit on " + id + " " + date);

		try {
			return Programme.Factory.parse(cacheFile);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error("Error loading file " + id, e);
			return null;
		}
	}

	public void saveInCache(Date date, String id, Programme program)
			throws CacheException {
		File dir = buildCachePath(date);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new CacheException("Unable to create cache folder " + dir.getName());
		}
		try {
			program.save(buildCacheFile(dir, id));
			if (log.isDebugEnabled())
				log.debug("Cache store of " + id);
		} catch (IOException e) {
			if (log.isErrorEnabled())
				log.error("Error saving file", e);
			throw new CacheException("Error saving file");
		}
	}

	private File buildCachePath(Date date) {
		return new File(new File(folder), CACHE_FOLDER_FORMAT.format(date));
	}

	private File buildCacheFile(File cacheFolder, String id) {
		return new File(cacheFolder, id + ".xml");
	}

	public Runnable createCleaner() {
		return new Runnable() {
			public void run() {
				cleanCache();
			}
		};
	}
	
	private void cleanCache() {

		File cacheDir = new File(this.folder);
		if (!cacheDir.exists()) {
			if (log.isDebugEnabled()) log.info(
								cacheDir.getName() + " does not exist, nothing to clean.");
			return;
		}
		
		File[] cacheFolders = cacheDir.listFiles(new ExpiredCacheFolderFileFilter());
		for (File cacheFolder : cacheFolders) {
			for (File file : cacheFolder.listFiles(new CacheFileFilter())) {
				file.delete();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if (!cacheFolder.delete()) {
				log.warn("Cannot clean cache folder " + cacheFolder.getAbsolutePath());
			}
		}
	
	}

	private static class CacheFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}
	}

	private static class ExpiredCacheFolderFileFilter implements FileFilter {
		private final Date today;
		private ExpiredCacheFolderFileFilter() {
			super();
			today =  createToDay();
		}
		private final Date createToDay() {
			try {
				return CACHE_FOLDER_FORMAT.parse(CACHE_FOLDER_FORMAT.format(new Date()));
			} catch (ParseException e) {
				// hate it when this happens ;-)
				return null;
			}
		}
		public boolean accept(File pathname) {
			if (!pathname.isDirectory()) return false;
			String fileName = pathname.getName();
			try {
				Date fileDate = CACHE_FOLDER_FORMAT.parse(fileName);
				return fileDate.before(today);
			} catch (ParseException e) {
				// not our file...
				return false;
			}
		}
	}
}
