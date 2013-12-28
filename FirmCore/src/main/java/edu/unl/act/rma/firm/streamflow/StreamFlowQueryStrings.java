package edu.unl.act.rma.firm.streamflow;

import java.io.ObjectStreamException;

public enum StreamFlowQueryStrings {
	DAILY_BASE("select day_num, month_num, year, value from daily inner join variable on daily.variable_id = variable.variable_id" +
			" inner join station on variable.station_id = station.station_id where station.station_id = ? and variable.variable_name = ?" +
			" and (year>? or (year=? and (month_num>? or (month_num=? and day_num>=?)))) and (year< ? or (year=? and (month_num<? or (month_num=? and day_num<=?))))" +
			" order by year, month_num, day_num"),
			
	WEEKLY_BASE("select week_num, year, value from weekly inner join variable on weekly.variable_id = variable.variable_id" +
			" inner join station on variable.station_id = station.station_id where station.station_id = ? and variable.variable_name = ?" +
			" and (year>? or (year=? and (week_num>=? ))) and (year< ? or (year=? and (week_num<=?))) order by year, week_num"),
			
	MONTHLY_BASE("select month_num, year, value from monthly inner join variable on monthly.variable_id = variable.variable_id" +
			" inner join station on variable.station_id = station.station_id where station.station_id = ? and variable.variable_name = ?" +
			" and (year>? or (year=? and (month_num>=? ))) and (year< ? or (year=? and (month_num<=?))) order by year, month_num"), 
	
//	updated ANNUAL_BASE query  jira FARM-445
 	ANNUAL_BASE("select MAX(unioned.value) from (select year, SUM(w.value) AS value FROM weekly w INNER JOIN variable v ON w.variable_id = v.variable_id" + 
			 " inner join station s on v.station_id = s.station_id  where v.station_id=? AND v.variable_name=? AND w.value <> -99" + 
		     " and (year>? or (year=? and (week_num>=?))) and (year<? or (year=? and (week_num<=?))) GROUP BY year HAVING count(value)>?" +
		     " UNION" +
		     " SELECT n3.num*100+n2.num*10+n1.num +? as year,-99 as value" +
		     " FROM (SELECT 0 AS num UNION ALL" +
		          " SELECT 1 UNION ALL" +
		          " SELECT 2 UNION ALL" +
		          " SELECT 3 UNION ALL" +
		          " SELECT 4 UNION ALL" +
		          " SELECT 5 UNION ALL" +
		          " SELECT 6 UNION ALL" +
		          " SELECT 7 UNION ALL" +
		          " SELECT 8 UNION ALL" +
		          " SELECT 9) n3" +
		    " ,(SELECT 0 AS num UNION ALL" +
		          " SELECT 1 UNION ALL" +
		          " SELECT 2 UNION ALL" +
		          " SELECT 3 UNION ALL" +
		          " SELECT 4 UNION ALL" +
		          " SELECT 5 UNION ALL" +
		          " SELECT 6 UNION ALL" +
		          " SELECT 7 UNION ALL" +
		          " SELECT 8 UNION ALL" +
		          " SELECT 9) n2" +
		    " ,(SELECT 0 AS num UNION ALL" +
		          " SELECT 1 UNION ALL" +
		          " SELECT 2 UNION ALL" +
		          " SELECT 3 UNION ALL" +
		          " SELECT 4 UNION ALL" +
		          " SELECT 5 UNION ALL" +
		          " SELECT 6 UNION ALL" +
		          " SELECT 7 UNION ALL" +
		          " SELECT 8 UNION ALL" +
		          " SELECT 9) n1" +
		    " order by 1" +
 			" ) as unioned GROUP BY year having year<=?"),
		     
		     
		     /*
		     " select year, -99 as value FROM weekly w INNER JOIN variable v ON w.variable_id = v.variable_id " +
		     " inner join station s on v.station_id = s.station_id  where v.station_id=? AND v.variable_name=? " +
		     " AND (year>? or (year=? and (week_num>=?))) and (year<? or (year=? and (week_num<=?))) GROUP BY year) as unioned GROUP BY year"),
*/
	/*
	 * Note ANNUAL_BASE_AVG is not the same query ad AVERAGE_ANNUAL_BASE
	 * This query is for temperature data and gives an average for each year in the date range, while
	 * AVERAGE_ANNUAL_BASE gives one average value for a station.
	 */	     
  	ANNUAL_BASE_AVG("select MAX(unioned.value) from (select year, AVG(w.value) AS value FROM weekly w INNER JOIN variable v ON w.variable_id = v.variable_id" + 
			 " inner join station s on v.station_id = s.station_id  where v.station_id=? AND v.variable_name=? AND w.value <> -99" + 
		     " and (year>? or (year=? and (week_num>=?))) and (year<? or (year=? and (week_num<=?))) GROUP BY year HAVING count(value)>?" +
		     " UNION" +
		     " SELECT n3.num*100+n2.num*10+n1.num +? as year,-99 as value" +
		     " FROM (SELECT 0 AS num UNION ALL" +
		          " SELECT 1 UNION ALL" +
		          " SELECT 2 UNION ALL" +
		          " SELECT 3 UNION ALL" +
		          " SELECT 4 UNION ALL" +
		          " SELECT 5 UNION ALL" +
		          " SELECT 6 UNION ALL" +
		          " SELECT 7 UNION ALL" +
		          " SELECT 8 UNION ALL" +
		          " SELECT 9) n3" +
		    " ,(SELECT 0 AS num UNION ALL" +
		          " SELECT 1 UNION ALL" +
		          " SELECT 2 UNION ALL" +
		          " SELECT 3 UNION ALL" +
		          " SELECT 4 UNION ALL" +
		          " SELECT 5 UNION ALL" +
		          " SELECT 6 UNION ALL" +
		          " SELECT 7 UNION ALL" +
		          " SELECT 8 UNION ALL" +
		          " SELECT 9) n2" +
		    " ,(SELECT 0 AS num UNION ALL" +
		          " SELECT 1 UNION ALL" +
		          " SELECT 2 UNION ALL" +
		          " SELECT 3 UNION ALL" +
		          " SELECT 4 UNION ALL" +
		          " SELECT 5 UNION ALL" +
		          " SELECT 6 UNION ALL" +
		          " SELECT 7 UNION ALL" +
		          " SELECT 8 UNION ALL" +
		          " SELECT 9) n1" +
		    " order by 1" +
		    " ) as unioned GROUP BY year having year<=?"),
		    
		     
		    /* 
		     " select year, -99 as value FROM weekly w INNER JOIN variable v ON w.variable_id = v.variable_id " +
		     " inner join station s on v.station_id = s.station_id  where v.station_id=? AND v.variable_name=? " +
		     " AND (year>? or (year=? and (week_num>=?))) and (year<? or (year=? and (week_num<=?))) GROUP BY year) as unioned GROUP BY year"),
		     	*/	     
		     
			/*SQL by Jon */

//	updated AVERAGE_MONTHLY_BASE query  jira FARM-445			
	AVERAGE_MONTHLY_BASE("SELECT unioned.month_num, max(unioned.value) FROM " +
			" (SELECT m.month_num, AVG(m.value) AS value FROM monthly m " +
			" INNER JOIN variable v ON m.variable_id = v.variable_id WHERE v.station_id=? " +
			" AND v.variable_name=? AND m.value <> -99 GROUP BY m.month_num " +
			" UNION " +
			" SELECT distinct ID as month_num, -99 AS value FROM help_union  where ID<13" +
			" ) as unioned GROUP BY month_num ORDER BY month_num"),
			
//	updated AVERAGE_WEEKLY_BASE query  jira FARM-445		
	AVERAGE_WEEKLY_BASE("SELECT unioned.week_num, max(unioned.value) FROM" +
			" (SELECT w.week_num, AVG(w.value) AS value FROM weekly w INNER JOIN variable v ON w.variable_id = v.variable_id " +
			" WHERE v.station_id=? AND v.variable_name=? AND w.value <> -99 GROUP BY w.week_num " +
			" UNION " +
			" SELECT ID as week_num, value AS value FROM help_union  where ID<53" +
			" )" +
			" as unioned GROUP BY week_num ORDER BY week_num"), 
			
//	updated AVERAGE_DAILY_BASE query  jira FARM-445
	AVERAGE_DAILY_BASE("SELECT unioned.day_num, unioned.day_num, max(unioned.value) FROM" +
			" (SELECT d.month_num, d.day_num, AVG(d.value) AS value FROM daily d INNER JOIN variable v ON d.variable_id = v.variable_id " +
			" WHERE v.station_id=? AND v.variable_name=? AND d.value <> -99 AND d.value <> -100 GROUP BY d.month_num, d.day_num " +
			" UNION" +
			" SELECT month_num, day_num, value from help_union_year" +
			" ) as unioned " +
			" GROUP BY month_num, day_num ORDER BY month_num, day_num"), 

//	updated AVERAGE_ANNUAL_BASE query  jira FARM-445
	AVERAGE_ANNUAL_BASE("SELECT max(unioned.value) FROM " +
			" (SELECT AVG(m.value) AS value FROM monthly m INNER JOIN variable v ON m.variable_id = v.variable_id " +
			" WHERE v.station_id=? AND v.variable_name=? AND m.value <> -99" +
			" UNION" +
			" select -99 as value from help_union where ID<2" +
			" ) as unioned ");
			
	private String queryString;
	
	private StreamFlowQueryStrings(String str) { 
		queryString = str;
	}
	
	public String getQueryString() { 
		return queryString;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
