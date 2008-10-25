package org.anuta.xmltv.transport;
/**
 * SVN TEST
 */
import org.anuta.xmltv.exceptions.TransportException;

/**
 * Simple interface for transport.
 * 
 * @author afedorov
 */
public interface Transport {
    /**
     * Return text document for given url
     * 
     * @return text dicument
     * @throws TransportException
     */
    public String getText(String url) throws TransportException;
}
