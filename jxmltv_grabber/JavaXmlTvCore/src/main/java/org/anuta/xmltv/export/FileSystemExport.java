package org.anuta.xmltv.export;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.server.ExportException;

import org.apache.xmlbeans.XmlObject;

public class FileSystemExport implements Export {
    private String fileName;

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public void export(XmlObject xml) throws ExportException {
	try {
	    File f = new File(getFileName());
	    
	    xml.save(f);
	  
	} catch (FileNotFoundException e) {
	    throw new ExportException("File not found exception " + e.getMessage());
	} catch (IOException e) {
	    throw new ExportException("IO exception " + e.getMessage());
	}
    }
}
