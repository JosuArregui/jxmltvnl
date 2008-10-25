package org.anuta.xmltv.export;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.server.ExportException;

import org.apache.xmlbeans.XmlObject;

public class ConsoleExport implements Export {
    public void export(XmlObject xml) throws ExportException {
	try {
	    xml.save(System.out);
	} catch (FileNotFoundException e) {
	    throw new ExportException("File not found exception " + e.getMessage());
	} catch (IOException e) {
	    throw new ExportException("IO exception " + e.getMessage());
	}
    }
}
