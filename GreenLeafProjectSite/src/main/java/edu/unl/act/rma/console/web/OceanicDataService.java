/**
 * 
 */
package edu.unl.act.rma.console.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.CalendarDataParser;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.LogManager;

/**
 * @author bkutsch
 *
 */
public class OceanicDataService {
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, OceanicDataService.class);

	private static DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.OCEANIC);
	
	private final static String OCEAN_INSERT_SQL="INSERT INTO monthly( variable_id, year, month, value, derived) VALUES(?,?,?,?,?)";
	private final static String OCEAN_WEEKLY_INSERT_SQL="INSERT INTO weekly( variable_id, year, month, week, value, derived) VALUES(?,?,?,?,?,?)";
	
	private final static String UPDATE_START_DATE="UPDATE oceanic_variable set start_date=? where name=?";
	private final static String UPDATE_END_DATE="UPDATE oceanic_variable set end_date=? where name=?";
	
	private static int SOI_ANOMALY =1;
	private static int MEI =2;
	private static int NAM_PC1 =3;
	private static int PNA_PC2 =4;
	private static int PC3 =5;
	private static int NAO =6;
	private static int ONI =7;
	private static int NPI =8;
	private static int PDO =9;
	private static int JMASST =10;
	private static int SOI_STANDARD	=11;
	private static int AMO= 12;
	private static int MJORMM1=13;
	private static int MJORMM2=14;
	
	public void create_tables(){
		
		String sqlDropMonthly="drop table if exists `monthly`";
		String sqlDropOceanicVariable="drop table if exists `oceanic_variable`";
		String sqlDropType="drop table if exists `type`";
		String sqlDropWeekly="drop table if exists `weekly`";
			
		String sqlCreateMonthly="CREATE TABLE `monthly` (" +
				" `monthly_id` int(11) NOT NULL auto_increment," +
				" `variable_id` int(11) NOT NULL," +
				" `year` int(11) NOT NULL," +
				" `month` int(11) NOT NULL," +
				" `value` decimal(11,5) NOT NULL," +
				"`derived` bit(1) NOT NULL default '\0'," +
				" PRIMARY KEY  (`monthly_id`)" +
				" ) ENGINE=MyISAM AUTO_INCREMENT=27270 DEFAULT CHARSET=latin1";
		
		String sqlCreateWeekly="CREATE TABLE `weekly` (" +
				" `weekly_id` int(11) NOT NULL auto_increment," +
				" `variable_id` int(11) NOT NULL," +
				" `year` int(11) NOT NULL," +
				" `month` int(11) NOT NULL," +
				" `week` int(11) NOT NULL," +
				" `value` decimal(11,5) NOT NULL," +
				"`derived` bit(1) NOT NULL default '\0'," +
				" PRIMARY KEY  (`weekly_id`)" +
				" ) ENGINE=MyISAM AUTO_INCREMENT=27270 DEFAULT CHARSET=latin1";

		String sqlCreateOcenaciVariable="CREATE TABLE `oceanic_variable` (" +
				" `variable_id` int(11) NOT NULL auto_increment," +
				" `name` varchar(255) NOT NULL default '', " +
				" `description` varchar(255) NOT NULL default ''," +
				" `start_date` date NOT NULL," +
				" `end_date` date NOT NULL," +
				" `type_id` int(11) NOT NULL," +
				" `source_url` varchar(255) NOT NULL default ''," +
				" PRIMARY KEY  (`variable_id`)" +
				" ) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=latin1";
		
		String sqlCreateType="CREATE TABLE `type` (" +
				" `type_id` int(11) NOT NULL auto_increment," +
				" `type` varchar(255) NOT NULL default ''," +
				" PRIMARY KEY  (`type_id`)" +
				" ) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1";
		
		Connection conn=null;
		try{
			conn= source.getConnection();
			
			conn.createStatement().execute(sqlDropMonthly);
			conn.createStatement().execute(sqlDropOceanicVariable);
			conn.createStatement().execute(sqlDropType);
			conn.createStatement().execute(sqlDropWeekly);
			conn.createStatement().execute(sqlCreateMonthly);
			conn.createStatement().execute(sqlCreateOcenaciVariable);
			conn.createStatement().execute(sqlCreateType);
			conn.createStatement().execute(sqlCreateWeekly);
			
			/*
			 * The following values are hardcoded, but the start and end dates are updated as part of the screen scraping process
			 */
			
			conn.createStatement().execute("insert into `oceanic_variable` values('1','SOI_ANOMALY','Southern Oscillation Index-Anomaly','1951-01-01','2006-12-31','1','http://www.cpc.ncep.noaa.gov/data/indices/soi')");
			conn.createStatement().execute("insert into `oceanic_variable` values('2','MEI','Multivariate ENSO Index','1950-01-01','2006-09-30','2','http://www.cdc.noaa.gov/people/klaus.wolter/MEI/table.html')");
			conn.createStatement().execute("insert into `oceanic_variable` values('3','NAM_PC1','Northern Annular Mode','1948-01-01','2006-03-31','1','http://jisao.washington.edu/analyses0302/slpanompc.ascii')");
			conn.createStatement().execute("insert into `oceanic_variable` values('4','PNA_PC2','Pacific / North American (PNA) index','1948-01-01','2006-03-31','1','http://jisao.washington.edu/analyses0302/slpanompc.ascii')");
			conn.createStatement().execute("insert into `oceanic_variable` values('5','PC3','PC3','1948-01-01','2006-03-31','1','http://jisao.washington.edu/analyses0302/slpanompc.ascii')");
			conn.createStatement().execute("insert into `oceanic_variable` values('6','NAO','North Atlantic Oscillation','1950-01-01','2005-05-31','1','http://www.cpc.noaa.gov/products/precip/CWlink/pna/norm.nao.monthly.b5001.current.ascii.table')");
			conn.createStatement().execute("insert into `oceanic_variable` values('7','ONI','Oceanic Niño Index','1950-01-01','2006-08-31','3','http://www.cpc.noaa.gov/products/analysis_monitoring/ensostuff/ensoyears.shtml')");
			conn.createStatement().execute("insert into `oceanic_variable` values('8','NPI','North Pacific Index','1899-01-01','2006-03-31','1','http://www.cgd.ucar.edu/cas/jhurrell/indices.data.html#npmon')");
			conn.createStatement().execute("insert into `oceanic_variable` values('9','PDO','The Pacific Decadal Oscillation','1900-01-01','2006-09-30','1','http://jisao.washington.edu/pdo/PDO.latest')");
			conn.createStatement().execute("insert into `oceanic_variable` values('10','JMASST','JMA Sea Surface Anomally','1949-01-01','2006-12-31','1','ftp://www.coaps.fsu.edu/pub/JMA_SST_Index/')");
			conn.createStatement().execute("insert into `oceanic_variable` values('11','SOI_STANDARD','Southern Oscillation Index-Standard','1951-01-01','2006-12-31','1','http://www.cpc.ncep.noaa.gov/data/indices/soi')");
			conn.createStatement().execute("insert into `oceanic_variable` values('12','AMO','Atlantic Multidecadal Oscillation','1856-01-01','2007-12-31','1','http://www.cdc.noaa.gov/Correlation/amon.us.long.data')");
			conn.createStatement().execute("insert into `oceanic_variable` values('13','RMM1',' Real-time Multivariate MJO series 1','1974-06-01','2007-5-28','4','http://www.bom.gov.au/bmrc/clfor/cfstaff/matw/maproom/RMM/RMM1RMM2.74toRealtime.txt')");
			conn.createStatement().execute("insert into `oceanic_variable` values('14','RMM2',' Real-time Multivariate MJO series 2','1974-06-01','2007-5-28','4','http://www.bom.gov.au/bmrc/clfor/cfstaff/matw/maproom/RMM/RMM1RMM2.74toRealtime.txt')");
			
			conn.createStatement().execute("insert into `type` values('1','monthly')");
			conn.createStatement().execute("insert into `type` values('2','monthly 2-month avg')");
			conn.createStatement().execute("insert into `type` values('3','monthly 3-month avg')");	
			conn.createStatement().execute("insert into `type` values('4','weekly')");	
			
		}catch(SQLException e){
			LOG.error("SQL excpetion",e);
		}finally{
			try{
				conn.close();
			}catch(SQLException e){
				LOG.error("SQL excpetion when closing the connection",e);
			}
		}
	}
	
	public void loaddata(){
		for(OceanicDataURL url :OceanicDataURL.values()){
			try{
				URLConnection ucon= new URL(url.getUrl()).openConnection();
				InputStreamReader isr=new InputStreamReader(ucon.getInputStream());
				BufferedReader buff=new BufferedReader(isr);
				String line=buff.readLine();
				int type=0;
				boolean start=false;
	    		//NAM patterns
				Pattern pNam = Pattern.compile("-?\\d.\\d\\d\\d\\d\\d\\d\\de(\\+|-)\\d\\d");
	    		
				//ONI patterns
	    		Pattern py=Pattern.compile("\\d\\d\\d\\d");
				Pattern pv=Pattern.compile("-?\\d.\\d");
				Pattern pYear = Pattern.compile("<p align=\"center\"><font face=\"verdana,arial\" size=\"2\"><strong>\\d\\d\\d\\d</strong>.*");
				Pattern pBlueVal = Pattern.compile("<p align=\"center\"><font face=\"verdana,arial\" size=\"2\">(<strong><span style=\"color:blue\">|<span style=\"color:blue\"><strong>)-\\d.\\d.*");
				Pattern pRedVal = Pattern.compile("<p align=\"center\"><font face=\"verdana,arial\" size=\"2\"> ?(<span style=\"color:red\"><strong>|<strong><span style=\"color:red\">)\\d.\\d.*");
				Pattern pVal = Pattern.compile("<p align=\"center\"><font face=\"verdana,arial\" size=\"2\">-?\\d.\\d.*");
	    		
	    		Integer year=0;
	    		DateTime startdate = null;
	    		DateTime enddate=null;
	    		List<String> alrm1= new ArrayList<String>();
	    		List<String> alrm2= new ArrayList<String>();
	    		int month=1;
	    		
	    		Matcher matcherValue;
	    		int rmcount = 0 ; 
	    		if(url.name().equals("MJORMM1")||
	    				url.name().equals("MJORMM2")){
	    			line=buff.readLine();
					line=buff.readLine();
	    		}
	    		if(url.name().equals("AMO")){
	    			line=buff.readLine();
					
	    		}
				while(line!=null){
					line=line.trim();
					
					switch (url){
					
				    	case SOI:
							if (line.contains("ANOMALY")){
								type=SOI_ANOMALY;
							}
							if (line.contains("STANDARDIZED")){
								type=SOI_STANDARD;
							}
							try{
								year = new Integer(line.substring(0, 4));
								insertline(year, line, type);
							}catch(Exception e){/* ignore-- These exceptions occur on lines that are ignored */ }
				    		break;
				    		
				    	case MEI:
							type=MEI;
							try{
								year = new Integer(line.substring(0, 4));
								insertline(year, line, type);
							}catch(Exception e){/* ignore*/}
				    		break;
				    		
				    	case NAM:
							Double [] input= new Double[5];
							int count=0;
							matcherValue= pNam.matcher(line);
							while(matcherValue.find()){
								input[count]= Double.parseDouble(matcherValue.group());
								count++;
							}
							insertNAM(input);
				    		break;
				    		
				    	case NAO:
							type=NAO;
							
							try{
								year = new Integer(line.substring(0, 4));
								insertline(year, line, type);
							}catch(Exception e){/* ignore*/}
				    		break;
			    		
				    	case ONI:
				    		type=ONI;
				    		
							Matcher mONI = pYear.matcher(line);
							boolean bYear = mONI.matches();
							if (bYear) {
								matcherValue= py.matcher(line);
								if(matcherValue.find()){
									year=new Integer(matcherValue.group());
							  		month=1;
								}
							}
							  
							Matcher mBlue = pBlueVal.matcher(line);
							boolean bBlue = mBlue.matches();
							if (bBlue) {
								matcherValue= pv.matcher(line);
								if(matcherValue.find()){
									insertONI(year,month, matcherValue.group());
									month++;
								}
							}
							  
							Matcher mRed = pRedVal.matcher(line);
							boolean bRed = mRed.matches();
							if (bRed) {
								matcherValue= pv.matcher(line);
								if(matcherValue.find()){
									insertONI(year,month, matcherValue.group());
									month++;
								}
							}
							Matcher mVal = pVal.matcher(line);
							boolean bVal = mVal.matches();
							if (bVal) {
								matcherValue= pv.matcher(line);
								if(matcherValue.find()){
									insertONI(year,month, matcherValue.group());
									month++;
								}
							}
				    		break;
				    		
				    	case NPI:
				    		type=NPI;
				    		if(line.equals("<h3 class=\"bluefill\">Monthly North Pacific Index</h3>")){ 			
				    			start=true;
				    		}
				    		//check for start
				    		if(start){
								try{
									year = new Integer(line.substring(0, 4));
									insertline(year, line, type);
								}catch(Exception e){/* ignore*/}
				    		}
				    		//check for end
				    		if(line.equals("<h4 align=right><a href=\"npindex.html\">Return to Climate Indices Table</a></h4>")){
				    			start=false;
				    		}
				    		break;
				    		
				    	case PDO:
							type=PDO;
							try{
								year = new Integer(line.substring(0, 4));
								insertline(year, line, type);
							}catch(Exception e){/* ignore*/}
				    		break;
				    		
				    	case JMASST:
							type=JMASST;
							try{
								year = new Integer(line.substring(0, 4));
								insertline(year, line, type);
							}catch(Exception e){/* ignore*/}
				    		break;
				    	case AMO:
				    		type=AMO;
				    		if(line.equals("-99.99")){
								break;
							}
							try {
								year = new Integer(line.substring(0, 4));
								insertline(year, line, type );
							} catch (NumberFormatException e) {/*ignore*/	}
							break;
				    	case MJORMM1:
				    		type= MJORMM1;
				    		line=line.trim();
							String data[]= line.split("\\s+");
							int rmyear = new Integer(line.substring(0, 4));
							int rmmonth=new Integer(data[1]);
							int rmday=new Integer(data[2]);
							if (data[3].equals("1.00000E+36")){ // replace missing data with the standard firm missing data value
								Float missing =new Float(DataType.MISSING);
								alrm1.add(missing.toString());
							}else{
								alrm1.add(data[3]);
							}
							if(rmcount==0){
								startdate= new DateTime(rmyear,rmmonth,rmday,0, 0, 0, 0, GregorianChronology.getInstance());
							}
							rmcount++;
							enddate= new DateTime(rmyear,rmmonth,rmday,0, 0, 0, 0, GregorianChronology.getInstance());
							break;
				    	case MJORMM2:
				    		type= MJORMM2;
				    		line= line.trim();
							String data1[]= line.split("\\s+");
							int rm2year = new Integer(line.substring(0, 4));
							int rm2month=new Integer(data1[1]);
							int rm2day=new Integer(data1[2]);
							if (data1[4].equals("1.00000E+36")){  // replace missing data with the standard firm missing data value
								Float missing =new Float(DataType.MISSING);
								alrm2.add(missing.toString());
							}else{
								alrm2.add(data1[4]);
							}
							if(rmcount==0){
								startdate= new DateTime(rm2year,rm2month,rm2day,0, 0, 0, 0, GregorianChronology.getInstance());
							}
							rmcount++;
							enddate= new DateTime(rm2year,rm2month,rm2day,0, 0, 0, 0, GregorianChronology.getInstance());
							break;
						
					}
					line=buff.readLine();
				}
				
				switch (url){
				    	case SOI:
				    		break;
				       	case MEI:
				    		break;
				       	case NAM:
				    		break;
				       	case NAO:
				    		break;
			    	   	case ONI:
				    		break;
				       	case NPI:
				    		break;
				       	case PDO:
				    		break;
				       	case JMASST:
				    		break;
				    	case AMO:
				    		break;
				    	case MJORMM1:
				    		saveMJOdata(alrm1,MJORMM1,startdate);
				    		break;
				    	case MJORMM2:
				    		saveMJOdata(alrm2,MJORMM2,startdate);
				    		break;
				}
			}
			catch(MalformedURLException e){
				LOG.error("MalformedURLException excpetion",e);
			}
			catch(IOException e){
				LOG.error("IO excpetion",e);
			}
		}
		setDates();
	}
	
	private void saveMJOdata(List<String> alrm,int type,DateTime startdate){
		
		float[] rmm = new float[alrm.size()];
		int j = 0 ; 
		for(String rm1:alrm){
			rmm[j]= new Float(rm1);
			j++;
		}
		CalendarDataParser parserrmm = new CalendarDataParser(rmm, startdate.getYear(), startdate.getMonthOfYear(), startdate.getDayOfMonth());
		int starting_doy = startdate.getDayOfYear() - 1;
		Connection c=null;
		try{

			c= source.getConnection();		
			PreparedStatement ps = c.prepareStatement(OCEAN_WEEKLY_INSERT_SQL);
			while (parserrmm.getDayOfYear() < starting_doy) {
				parserrmm.nextDay();
			}
			
			while (parserrmm.hasNextWeek() ) {
				parserrmm.nextWeek();
				ps.setInt(1,type);
				ps.setInt(2,parserrmm.getYear(CalendarPeriod.WEEKLY));
				ps.setInt(3,parserrmm.getMonthOfYear());
				ps.setInt(4,parserrmm.getWeekOfYear());
				Float roundedaverage= new Float(Math.round(parserrmm.weekAverage()*100000.0)/100000.0); // round to 5 places
				ps.setString(5,Float.toString(roundedaverage));
				ps.setBoolean(6, false);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch(SQLException e){
			LOG.error("SQL excpetion",e);
		}finally{
			try{
				c.close();
			}catch(SQLException e){
				LOG.error("SQL excpetion when closing the connection",e);
			}
		}
	}
	
	private static void insertline(Integer year, String line, int type){
		if(type!=0){
			boolean derived=false;
			if(type==AMO){
				line=line.replaceAll("-99.990", "-99.0");
			}
			if (type==SOI_ANOMALY || type ==SOI_STANDARD){
				line=line.replaceAll("-999.9", " -999.9");
			}
			if( type==MEI ){
				line=line.replace("\t", " ");
			}
			if(type==NPI){
				line=line.replaceAll("-99.", "-99.0");
			}
			if(type==PDO){
				if(line.contains("**")){
					derived=true;
				}
				line=line.replaceAll("\\*", "");
			}
			
			String data[]= line.split("\\s+");
			int count=0;
			Connection c=null;
			try{

				c= source.getConnection();		
				PreparedStatement ps = c.prepareStatement(OCEAN_INSERT_SQL);
				for(String s : data){
					if (!s.equals("-999.9")){	// SOI empty value check
						if ( !s.trim().equals("") && !s.equals(year.toString()) ){
							count++;
							ps.setInt(1,type);
							ps.setInt(2,year);
							ps.setInt(3,count);
							ps.setString(4,s);
							ps.setBoolean(5, derived);
						
							ps.addBatch();
						}
					}
				}
				ps.executeBatch();
			}catch(SQLException e){
				LOG.error("SQL excpetion",e);
			}finally{
				try{
					c.close();
				}catch(SQLException e){
					LOG.error("SQL excpetion when closing the connection",e);
				}
			}
		}
	}
	
	private static void insertNAM(Double[] input){
		int year=input[0].intValue();
		int month=input[1].intValue();
		if(input[2]!=null){
			String pc1=input[2].toString();
			String pc2=input[3].toString();
			String pc3=input[4].toString();
			Connection c=null;
			try{
				
				c= source.getConnection();		
				PreparedStatement ps = c.prepareStatement(OCEAN_INSERT_SQL);

				ps.setInt(1,NAM_PC1);
				ps.setInt(2,year);
				ps.setInt(3,month);
				ps.setString(4,pc1);
				ps.setBoolean(5, false);
				ps.addBatch();

				ps.setInt(1,PNA_PC2);
				ps.setInt(2,year);
				ps.setInt(3,month);
				ps.setString(4,pc2);
				ps.addBatch();
				
				ps.setInt(1,PC3);
				ps.setInt(2,year);
				ps.setInt(3,month);
				ps.setString(4,pc3);
				ps.addBatch();

				ps.executeBatch();
			}catch(SQLException e){
				LOG.error("SQL excpetion",e);
			}finally{
				try{
					c.close();
				}catch(SQLException e){
					LOG.error("SQL excpetion when closing the connection",e);
				}
			}
		}
	}
	
	private static void insertONI(int year, int month, String value){
		Connection c=null;
		try{
			c= source.getConnection();		
			PreparedStatement ps = c.prepareStatement(OCEAN_INSERT_SQL);
			ps.setInt(1,ONI);
			ps.setInt(2,year);
			ps.setInt(3,month);
			ps.setString(4,value);
			ps.setBoolean(5, false);
			ps.addBatch();
			ps.executeBatch();
		}catch(SQLException e){
			LOG.error("SQL excpetion",e);
		}finally{
			try{
				c.close();
			}catch(SQLException e){
				LOG.error("SQL excpetion when closing the connection",e);
			}
		}
	}
	
	private void setDates(){
		
		DateTime start;
		DateTime end;
		
		Connection c=null;
		try{
			c= source.getConnection();		
			
			PreparedStatement psStart = c.prepareStatement(UPDATE_START_DATE);
			PreparedStatement psEnd = c.prepareStatement(UPDATE_END_DATE);
			
			List<DataType> ocean_var= new ArrayList<DataType>();
			ocean_var.add(DataType.SOI_ANOMALY);
			ocean_var.add(DataType.SOI_STANDARD);
			ocean_var.add(DataType.MEI);
			ocean_var.add(DataType.NAM_PC1);
			ocean_var.add(DataType.PNA_PC2);
			ocean_var.add(DataType.PC3);
			ocean_var.add(DataType.NAO);
			ocean_var.add(DataType.ONI);
			ocean_var.add(DataType.NPI);
			ocean_var.add(DataType.PDO);
			ocean_var.add(DataType.JMASST);
			ocean_var.add(DataType.AMO);
			ocean_var.add(DataType.RMM1);
			ocean_var.add(DataType.RMM2);
			
			for (DataType var: ocean_var){
	    		start = getStartDate(var);
	    		psStart.setDate(1, new java.sql.Date(start.toDateMidnight().getMillis()));
	    		psStart.setString(2, var.name());
	    		end = getEndDate(var);
	    		psEnd.setDate(1, new java.sql.Date(end.toDateMidnight().getMillis()));
	    		psEnd.setString(2, var.name());
	    		psStart.addBatch();
	    		psEnd.addBatch();
			}

    		psStart.executeBatch();
    		psEnd.executeBatch();
			
		}catch(SQLException e){
			LOG.error("SQL excpetion",e);
		}catch(InvalidArgumentException e){
			LOG.error("Tired to set date for unkown Type",e);
		}catch(RemoteException e){
			LOG.error("Couldn't get date",e);
		}finally{
			try{
				c.close();
			}catch(SQLException e){
				LOG.error("SQL excpetion when closing the connection",e);
			
			}
		}
	}
	
	private DateTime getStartDate(DataType variableID) throws RemoteException, InvalidArgumentException{
		String sql=null;
		if((variableID==DataType.RMM1)||(variableID==DataType.RMM2)){
			sql="SELECT month,year FROM weekly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"'ORDER BY year ASC, month ASC LIMIT 1";
		}else{
			sql="SELECT month,year FROM monthly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year ASC, month ASC LIMIT 1";
		}
		Connection conn = null;
		int month = 0;
		int year = 0;
		DateTime date=null;
		try{
			conn = source.getConnection();
			ResultSet rs=conn.createStatement().executeQuery(sql);
			while (rs.next()){
				month = rs.getInt("month");
				year = rs.getInt("year");
				date= new DateTime(year+"-"+month+"-01");
			}

			if(date==null){
				LOG.error("No date found for the given varaibleID: "+ variableID.name());
				throw new RemoteException("unable to query date for given variableID");
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query date: sql exception");
		} finally { 
			try {
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return date;
	}
	
	
	private DateTime getEndDate(DataType variableID) throws RemoteException, InvalidArgumentException{
		String sql=null;
		if((variableID==DataType.RMM1)||(variableID==DataType.RMM2)){
			sql="SELECT month,year FROM weekly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year DESC, month DESC LIMIT 1";
		}else{
			sql="SELECT month,year FROM monthly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year DESC, month DESC LIMIT 1";
		}
		Connection conn = null;
		int month = 0;
		int year = 0;
		int day =0;
		DateTime date=null;
		try{
			conn = source.getConnection();
			ResultSet rs=conn.createStatement().executeQuery(sql);
			while (rs.next()){
				month = rs.getInt("month");
				year = rs.getInt("year");
				date= new DateTime(year+"-"+month+"-01");
				day = date.dayOfMonth().getMaximumValue();
				date= new DateTime(year+"-"+month+"-"+day);
			}

			if(date==null){
				LOG.error("No date found for the given varaibleID: "+ variableID.name());
				throw new RemoteException("unable to query date for given variableID");
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query date: sql exception");
		} finally { 
			try {
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return date;
	}
	

}
