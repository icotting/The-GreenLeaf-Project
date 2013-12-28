package edu.unl.act.rma.firm.test;

public enum StationList {

	ASHLAND_ID("250375"), 			// ASHLAND 2, NE 1893-2005	changed to 2006	with Nov-06 databuild	
	PROVIDENCE_ID("376698"),  		// PROVIDENCE T F GREEN STATE ARP, RI 1932-current	changed name to PROVIDENCE, county Kent	
	INCOMPLETE_ID("378911"),		// WESTERLY 1 W, RI 1944-1950				
	SAINT_BERNARD("017157"),		// SAINT BERNARD, AL 1907-2005	changed to 2006	with Nov-06 databuild
	ADAMS_BEACH("080025"),			// ADAMS BEACH, FL 1956-1957			
	ASHLAND2_ID("250372"), 			// ASHLAND 2 NE, NE 1894-1908				
	ARTHUR("250369"),				// ARTHUR, NE 1982- current				
	DANBURY("061762"),				// DANBURY, CT 1937 - current -- missing data from 1986-1990	
	KRAMER("254540"),				// KRAMER, NE 1948-1978 precip only			
	KEARNEY4NE("254335"),			// KEARNEY 4 NE, NE 1894-current precip, 1895-current temp	
	AGATE("250030"),				// AGATE 3 E, NE 1900- current	
	EAST_WALLINGFORD("432682"),		// EAST WALLINGFORD #2, Vermont  2000-2004 
	AKRON("050119"),				// AKRON, CO 1983-10-13
	AKRON_WASHINGTON("050114"),		// AKRON WASHINGTON CO AP, CO 1937-current, changed name to AKRON with JAN 2007 build
	AKRON4E("050109"),				// AKRON 4 E, CO 1893-current
	DEL_NORTE("052184"),			// DEL NORTE 2E, CO 1893-current
	CALERA("011288"),				// CALERA, AL 1900-current
	COCHRANE("011799"),				// COCHRANE 2 E, AL 1909-1956; 
	BIRMINGHAM("00193"),			// BIRMINGHAM ALABASTER, AL 1995-04-01-current, changed to BIRMINGHAM with Jan 2007 build
	YORK("259519"),					// YORK, NE 1996-current, changed to 1996 with Jan 2007 build 
	WOLBACH("259382"),				// WOLBACH, NE 1992-current precip only
	WAYNE("229444"),			    // WAYNE, NE 1893-current precip 1989-current temp
	STRATTON("258255"),				// STRATTON, NE	1895-current precip 1919-1941 temp
	SIDNEY("257839"),				// SIDNEY, NE 1982-current changed to 2611425 with Jan 2007 build
	SHELTON("257779"),				// SHELTON, NE 1991-current
	SEWARD("257715"),				// SEWARD, NE 1893-current precip 1900-current
	POLK  ("256837"),				// POLK, NE 1949-current precip only
	PRAGUE("256922"),				// PRAGUE, NE 1994-current precip only
	MILLER("255525"),				// MILLER, NE 1906-current precip only
	LYMAN ("255020"),				// LYMAN, NE 1924-2006 precip only
	LYONS ("255050"),				// LYONS, NE 1895-current precip 1896-1901 temp
	HEBRON("253735"),				// HEBRON, NE 1893-current
	HAVELOCK ("254699"),			// HAVELOCK, NE 1983-current
	GENEVA("253175");				// GENEVA, NE 1893-2006

	
	private String stationID;

	private StationList(String stationID) { 
		this.stationID = stationID;
	}
	

	public String getStationID() {
		return stationID;
	}


	public void setStationID(String stationID) {
		this.stationID = stationID;
	}
}
