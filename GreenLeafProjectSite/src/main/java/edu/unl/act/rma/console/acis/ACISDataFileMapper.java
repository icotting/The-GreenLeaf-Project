/* Created On: Jun 28, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Ian Cottingham
 *
 */
public class ACISDataFileMapper {
	private HashMap<String, FileWriter> dailyWriters;
	private HashMap<String, FileWriter> weeklyWriters;
	private HashMap<String, FileWriter> monthlyWriters;
	
	private String filePath;
	private String variableId;
	
	public ACISDataFileMapper(String path, String variable) throws IOException {
		dailyWriters = new HashMap<String, FileWriter>();
		weeklyWriters = new HashMap<String, FileWriter>();
		monthlyWriters = new HashMap<String, FileWriter>();
				
		filePath = path;
		variableId = variable;
		
		createFiles();
	}
	
	public void cleanUp() throws IOException { 
		dailyWriters.get("99").close();
		weeklyWriters.get("99").close();
		monthlyWriters.get("99").close();
		
		/* create one file for each two year period of each decade from 1940 - 2039 */
		for ( int dec=0; dec<10; dec++ ) {				
			for ( int years = 0; years<5; years++ ) { 
				String str = String.valueOf(dec)+String.valueOf(years);
				dailyWriters.get(str).close();
				weeklyWriters.get(str).close();
				monthlyWriters.get(str).close();
			}
		}
	}
	
	public void createFiles() throws IOException { 
		dailyWriters.put("99", new FileWriter(filePath+"daily.99"));
		weeklyWriters.put("99", new FileWriter(filePath+"weekly.99"));
		monthlyWriters.put("99", new FileWriter(filePath+"monthly.99"));
		
		/* create one file for each two year period of each decade from 1940 - 2039 */
		for ( int dec=0; dec<10; dec++ ) {				
			for ( int years = 0; years<5; years++ ) { 
				String str = String.valueOf(dec)+String.valueOf(years);
				dailyWriters.put(str, new FileWriter(filePath+"daily."+str));
				weeklyWriters.put(str, new FileWriter(filePath+"weekly."+str));
				monthlyWriters.put(str, new FileWriter(filePath+"monthly."+str));
			}
		}
		
	}
	
	public void writeDaily(int year, int month, int day, float value) throws IOException {
		String period = getPeriod(year);		
		FileWriter daily = dailyWriters.get(period);
		daily.write(variableId+","+year+","+month+","+day+","+value+"|");
		daily.flush();
	}
	
	
	public void writeWeekly(int year, int week, float value) throws IOException { 
		String period = getPeriod(year);		
		FileWriter weekly = weeklyWriters.get(period); 	
		weekly.write(variableId+","+year+","+week+","+value+"|");
		weekly.flush();
	}
	
	public void writeMonthly(int year, int month, float value) throws IOException { 
		String period = getPeriod(year);		
		FileWriter monthly = monthlyWriters.get(period);	
		monthly.write(variableId+","+year+","+month+","+value+"|");
		monthly.flush();
	}
	
	private String getPeriod(int year) { 
		if ( year < 1940 )
			return "99";
		else { 
			int major = ( year % 100 ) / 10;
			int minor = ( year % 5 );
			return String.valueOf(major)+String.valueOf(minor);
		}
	}
	
}
