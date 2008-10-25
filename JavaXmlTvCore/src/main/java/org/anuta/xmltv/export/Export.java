package org.anuta.xmltv.export;

import java.rmi.server.ExportException;

import org.apache.xmlbeans.XmlObject;

public interface Export {
    public void export(XmlObject xml) throws ExportException;
}
