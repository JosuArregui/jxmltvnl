package org.anuta.xmltv;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Starter {

    public static void main(String[] args) {
	ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("tvgids.xml");
	XMLTVGrabber cm = (XMLTVGrabber) ctx.getBean("xmltvGrabber");
	cm.grab();
    }
}
