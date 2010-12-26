package org.anuta.xmltv.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.anuta.xmltv.beans.Program;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestOverlap extends TestCase {
    
    private static final Log log = LogFactory.getLog(TestOverlap.class);

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    
    private int getMaxOverlap() {
	return 300;
    }
    
    private int getOverlapFixMode() {
	return 0;
    }
    
    private void dumpList(String msg, List progs) {
	System.out.println(msg);
	Iterator it = progs.iterator();
	while (it.hasNext()) System.out.println(it.next());
    }
    
    
    private List getIntersectingPrograms(Program program, List programs) {
	List intersecting = new ArrayList();
	Iterator it = programs.iterator();
	while (it.hasNext()) {
	    Program p2 = (Program)it.next();
	    if (p2.equals(program)) continue;
	    if (p2.getStartDate().after(program.getEndDate()) && p2.getEndDate().after(program.getEndDate()) &&
	       p2.getEndDate().before(program.getStartDate()) && p2.getStartDate().before(program.getStartDate())) continue;
	    intersecting.add(p2);
	}
	return intersecting;
    }
    
    
    private void adjustTiming(List programs) {
	if ((programs == null) || (programs.size() < 2))
	    return;
	
	
	Iterator it = programs.iterator();
	while (it.hasNext()) {
	   ParentProgram curProgram = (ParentProgram)it.next();
	   List intersecting = getIntersectingPrograms(curProgram, programs);
	   if (intersecting.size()>0) {
	       dumpList("INTERSECTION OF "+curProgram, intersecting);
	   }
 	    
	}	    
	
	


	for (int i = 0; i < (programs.size() - 1); i++) {
	    Program p1 = (Program) programs.get(i);
	    Program p2 = (Program) programs.get(i + 1);

	    if (p1.getEndDate().after(p2.getStartDate())) {
		if (log.isDebugEnabled())
		    log.debug("Overlap detected " + p1 + " " + p2);
		long overlap = p1.getEndDate().getTime() - p2.getStartDate().getTime();
		long overlapMinutes = Math.round(overlap / 60000);

		if (log.isDebugEnabled())
		    log.debug("Overlapped on " + overlapMinutes + " minutes");

		if (overlapMinutes <= getMaxOverlap()) {
		    if (log.isDebugEnabled())
			log.debug("Fix overlap with mode " + getOverlapFixMode());

		    if (0 == getOverlapFixMode()) {
			// middle
			overlap = overlap >> 1;
			p1.getEndDate().setTime(p1.getEndDate().getTime() - overlap);
			p2.setStartDate(p1.getEndDate());
		    } else if (2 == getOverlapFixMode()) {
			// p2 leading
			p1.setEndDate(p2.getStartDate());
		    } else if (1 == getOverlapFixMode()) {
			// p1 leading
			p2.setStartDate(p1.getEndDate());
		    } else
			throw new IllegalArgumentException("Overlap mode can be 0,1 or 2");
		} else {
		    if (log.isDebugEnabled())
			log.debug("Overlap is too long, lets mark it as special");
		    p1.setClumpIdx("0/2");
		    p2.setClumpIdx("1/2");

		}

	    }
	}
    }

    public void testOverlap() {
	try {
	InputStream is = new FileInputStream(new File("src/test/resources/SBS6.txt"));
	byte[] buff = new byte[4000];
	String guide = "";
	List progs = new ArrayList();
	    int len = is.read(buff);
	    guide = new String(buff);
	    //System.out.println(guide);
	    
	    StringTokenizer st = new StringTokenizer(guide, "\n");
	    while (st.hasMoreTokens()) {
		String line = st.nextToken();
		
		String[] arr = line.split("\\|");
		
		ParentProgram p = new ParentProgram();
		p.setStartDate(new Date(Long.parseLong(arr[0])));
		p.setEndDate(new Date(Long.parseLong(arr[1])));
		p.setTitle(arr[2]);
		
	
	        progs.add(p);
	    }
	    dumpList("BEFORE", progs);
	    adjustTiming(progs);
	    
	    dumpList("AFTER", progs);
	    
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	assertEquals(0, 0);
    }
    
    private static class ParentProgram extends Program {
	private ParentProgram parent = null;
	public ParentProgram getParent() {
	    return parent;
	}
	public void setParent(ParentProgram parent) {
	    this.parent = parent;
	}
	public String toString() {
	    String p = "";
	    if (getParent()!=null) p = "<-- PARENT: "+getParent().toString();
	    return super.toString()+p;
	}
    }
}
