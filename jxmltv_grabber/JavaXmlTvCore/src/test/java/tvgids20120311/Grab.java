package tvgids20120311;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.anuta.xmltv.XMLTVGrabberTask;
import org.anuta.xmltv.beans.Channel;
import org.anuta.xmltv.beans.Program;
import org.anuta.xmltv.exceptions.TransportException;
import org.anuta.xmltv.grabber.tvgidsnl.AbstractGrabber;
import org.anuta.xmltv.grabber.tvgidsnl.TVGidsJsonGrabber;
import org.anuta.xmltv.test.ClasspathTransport;

public class Grab extends TestCase {

	public void testGrab() throws TransportException {
		
		AbstractGrabber grabber = new TVGidsJsonGrabber();
		grabber.setTvgidsurl("20120311/");
		grabber.setTransport(new ClasspathTransport());
		// http://www.tvgids.nl/json/lists/programs.php?channels=1&day=1
		grabber.setListingQuery("channels{channel}-day{day}.json");
		Channel channel1 = new Channel();
		channel1.setChannelId("1");
		channel1.setGrabber(grabber);
		try {
			XMLTVGrabberTask g = new XMLTVGrabberTask();
			g.setChannel(channel1);
			g.run();
			System.out.println(g.getResult());
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void testAnother() throws TransportException {
		AbstractGrabber grabber = new TVGidsJsonGrabber();
		grabber.setTvgidsurl("20120311/");
		grabber.setTransport(new ClasspathTransport());
		// http://www.tvgids.nl/json/lists/programs.php?channels=1&day=1
		grabber.setListingQuery("channels{channel}-day{day}.json");
		Channel channel1 = new Channel();
		channel1.setChannelId("2");
		channel1.setGrabber(grabber);
		
		grabber.getPrograms(channel1, new Date(), 0);
	}
}
