package edu.unl.act.rma.console.web;

import java.io.ObjectStreamException;

public enum OceanicDataURL {
	SOI("http://www.cpc.ncep.noaa.gov/data/indices/soi"),
	MEI("http://www.cdc.noaa.gov/people/klaus.wolter/MEI/table.html"),
	NAM("http://jisao.washington.edu/analyses0302/slpanompc.ascii"),
	NAO("http://www.cpc.noaa.gov/products/precip/CWlink/pna/norm.nao.monthly.b5001.current.ascii.table"),
	ONI("http://www.cpc.noaa.gov/products/analysis_monitoring/ensostuff/ensoyears.shtml"),
	NPI("http://www.cgd.ucar.edu/cas/jhurrell/indices.data.html#npmon"),
	PDO("http://jisao.washington.edu/pdo/PDO.latest"),
	JMASST("ftp://www.coaps.fsu.edu/pub/JMA_SST_Index/jmasst1949-today.anom.txt"),
	AMO("http://www.cdc.noaa.gov/Correlation/amon.us.long.data"),
	MJORMM1("http://www.bom.gov.au/bmrc/clfor/cfstaff/matw/maproom/RMM/RMM1RMM2.74toRealtime.txt"),
	MJORMM2("http://www.bom.gov.au/bmrc/clfor/cfstaff/matw/maproom/RMM/RMM1RMM2.74toRealtime.txt");

	private String url;

	private OceanicDataURL(String url) { 
		this.url = url;
	}
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
