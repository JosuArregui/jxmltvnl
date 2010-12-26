package org.anuta.imdb;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.anuta.imdb.beans.MovieRating;

public class IMDBRatingParser {
    private IMDBDownloader downloader;
    private BufferedReader reader;
    private final static String START_LINE = "MOVIE RATINGS REPORT";
    private final static String END_LINE = "-----------------";
    private String encoding = "ISO-8859-1";
    private List prefixes = new ArrayList();
    
    public List getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List prefixes) {
        this.prefixes = prefixes;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public IMDBRatingParser() throws IOException {
	

    }

    
    public void init() throws IOException {
	
	this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDownloader().getUnzippedOutputFile())), getEncoding()));
	
	while (true) {
	   String line = reader.readLine();
	   if (START_LINE.equals(line.trim())) {
		reader.readLine();
		reader.readLine();
		break;
	   }
	}
    }

    /**
     * Close reader 
     */
    public void destroy() {
	if (reader!=null) {
	    try { reader.close(); } catch (Exception e) {}
	}
    }
    
    private int parseYear(String year) {
	if (year.length()<4) return 0;
	if (year.length()>4) year = year.substring(0, 4);
	int y = 0;
	if ("????".equals(year)) return 0;
	
	try {
	    y = Integer.parseInt(year);
	} catch (NumberFormatException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return y;
    }
    
    /**
     *       0.....2023      12   8.7  "'Allo 'Allo!" (1982) {Desperate Doings in the Dungeon (#5.1)}
     */
    /**
     * Read next rating object from the file
     * @return
     * @throws IOException
     */
    public MovieRating getNextRating() throws IOException {
	String line = reader.readLine();
	
	if (line==null) line=""; 
	if (line.length()==0) return null;
	if (line.startsWith(END_LINE)) return null;
	

	String rating = line.substring(24, 32).trim();
	if (rating.length()==0) rating="0";
	String alltitle = line.substring(32).trim();
	String title = "";
	String year = "";
	int p=1;
	do {
  	  p = alltitle.indexOf('(', p+1);
  	  if (p==-1) break;
  	  if (p>=alltitle.length()-4) {
  	      p=-1;
  	      break;
  	  }
  	  int k = 0;
  	  for (int x=1;x<5;x++) {
  	    char c = alltitle.charAt(p+x);
  	    if ("01234567890?".indexOf(c)!=-1) k++;
  	  }
  	  //System.out.println("K="+k);
  	  if (k==4) break;
  	 
	} while (true);
	
	String subtitle = "";
	//System.out.println("P="+p);
	if (p>0) {
	    title = alltitle.substring(0, p).trim();
	    int p2 = alltitle.indexOf(')', p);
	    if (p2>p) {
		year = alltitle.substring(p+1, p2);
	    }

	    int p3 = alltitle.indexOf('{', p2+1);
	    if (p3!=-1) {
		int p4 = alltitle.indexOf('(', p3+1);
		int p5 = alltitle.indexOf('}', p3+1);
		
		if (p4!=-1) {
		    subtitle = alltitle.substring(p3+1, p4).trim();
		} else if (p5!=-1) {
		    subtitle = alltitle.substring(p3+1, p5).trim();
		}
		
	    }
	} else {
	    title = alltitle;
	    year = "????";
	}
	// normalize title
	if ((title.startsWith("\"")) && (title.endsWith("\""))) {
	    title = title.substring(1, title.length()-1);
	}
	
	Iterator it = getPrefixes().iterator();
	while (it.hasNext()) {
	    String prefix = (String)it.next();
	    String fullPrefix = ", "+prefix;

	    if (title.endsWith(fullPrefix)) {
		    title = prefix+" "+title.substring(0, title.length()-fullPrefix.length());
		    break;
	    }
	}

	MovieRating rat = new MovieRating();
	rat.setId(0);
	rat.setTitle(title);
	rat.setSubtitle(subtitle);
	rat.setYear(parseYear(year));
	
	try {
	    rat.setRating(new BigDecimal(rating));
	} catch (RuntimeException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	
	
	return rat;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public IMDBDownloader getDownloader() {
        return downloader;
    }

    public void setDownloader(IMDBDownloader downloader) {
        this.downloader = downloader;
    }
}
