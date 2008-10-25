package org.anuta.xmltv.test;

import junit.framework.TestCase;

import org.anuta.xmltv.XMLTVGrabber;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test extends TestCase {

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testFull() {
	try {
	    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("tvgids-test.xml");
	    XMLTVGrabber cm = (XMLTVGrabber)ctx.getBean("xmltvGrabber");
	    cm.grab();
	    
//	    HTTPTransport tr = (HTTPTransport)ctx.getBean("httpTransport");
//	    String s = tr.getText("http://www.tvgids.nl/programma/7419041/Der_Gro%DFe_Finanz-Check/");
//	    System.out.println(s);
//	    
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
        assertEquals(0, 0);
    }
}
