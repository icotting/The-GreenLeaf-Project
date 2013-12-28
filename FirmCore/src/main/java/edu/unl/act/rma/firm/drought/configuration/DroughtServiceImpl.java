/* Created on: Apr 22, 2010 */
package edu.unl.act.rma.firm.drought.configuration;

import edu.unl.act.rma.firm.core.*;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.jmx.Service;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePointParameter;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.USState;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Ian Cottingham
 *
 */
@Service(objectName="edu.unl.firm:type=DroughtService", providerInterface=DroughtServiceMBean.class)
public class DroughtServiceImpl implements DroughtServiceMBean, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, DroughtServiceImpl.class);
	private final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
	private final String BASE_URL = "http://torka.unl.edu/DroughtMonitor/Export/?mode=table&aoi=";
	private final DateTimeFormatter URL_DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
	
	private static final DateTimeFormatter KML_URL_FORMAT = DateTimeFormat.forPattern("yyMMdd");
	
	
	private String importerStatus = "Not Running";
	
	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	@Override
	public void importDroughtMonitorData(@ServicePointParameter(name="startDate", description="First map date to import")  final String startDate,
			@ServicePointParameter(name="endDate", description="Last map date to import")  final String endDate) {

		final DateTime start_date = FORMATTER.parseDateTime(startDate);
		final DateTime end_date = FORMATTER.parseDateTime(endDate);		
		getDmDate(start_date, end_date);
	}

    @Override
    public void importAllDroughtMonitorData() throws ConfigurationException {
        DateTime to_date = new DateTime(System.currentTimeMillis());
        if ( to_date.getDayOfWeek() < DateTimeConstants.TUESDAY ) {
            // this week's map is not up yet, so use last Tuesday
            to_date = to_date.minusWeeks(1);
        }
        
        to_date = to_date.minusDays(to_date.getDayOfWeek() - DateTimeConstants.TUESDAY);
        DateTime from_date;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = source.getConnection();
            rs = conn.createStatement().executeQuery("select max(map_date) from DroughtMonitorDescriptors");
            if ( !rs.next() ) {
                from_date = new DateTime(2000,1,4,0,0,0,0, GregorianChronology.getInstance());
            } else {
                from_date = new DateTime(rs.getDate(1)).plusWeeks(1);
            }

            getDmDate(from_date, to_date);
        } catch ( Exception e ) {
            LOG.error("An error occurred getting the last dm map date", e);
            ConfigurationException ce = new ConfigurationException("");
            ce.initCause(e);
            throw ce;
        } finally {
            try {
                rs.close();
                conn.close();
            } catch ( Exception ex ) {
                LOG.warn("A connection could not be closed", ex);
            }
        }
    }

	@Override
	public String getImporterStatus() throws ConfigurationException {
		return this.importerStatus;
	}	
	
	private void processUSTotals(long descriptorId, DateTime currentDate, PreparedStatement descriptorInsert) throws Exception { 
		
		URL u = new URL(BASE_URL+"us&date="+URL_DATE_FORMAT.print(currentDate));
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		
		ByteArrayInputStream bais; 
		if ( conn.getResponseCode() != HttpURLConnection.HTTP_OK ) {
			throw new RuntimeException("An error occurred connecting to the URL, the response code was: "+conn.getResponseCode());
		} else { // read in the full contents of the file before parsing it - this will prevent long held connections being reset
			InputStream in = conn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len; 
			while ( (len = in.read(b, 0, 1024)) > 0 ) { 
				baos.write(b, 0, len);
			}
			conn.disconnect();
			bais = new ByteArrayInputStream(baos.toByteArray());
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
			String line = reader.readLine(); // chop the header
			line = reader.readLine(); // this line contains the US data
			
			String[] tokens = line.split(",");

			descriptorInsert.setFloat(1, Float.parseFloat(tokens[3]));
			descriptorInsert.setFloat(2, Float.parseFloat(tokens[4]));
			descriptorInsert.setFloat(3, Float.parseFloat(tokens[5]));
			descriptorInsert.setFloat(4, Float.parseFloat(tokens[6]));
			descriptorInsert.setFloat(5, Float.parseFloat(tokens[7]));
			descriptorInsert.setFloat(6, Float.parseFloat(tokens[2]));
			descriptorInsert.setLong(7, descriptorId);
			descriptorInsert.setLong(8, SpatialReferenceType.US.ordinal());
			descriptorInsert.setNull(9, Types.BIGINT);
			descriptorInsert.setNull(10, Types.INTEGER);
			
			descriptorInsert.execute();
		} catch ( SQLException sqe ) { 
			throw sqe;
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing the US totals for date "+FORMATTER.print(currentDate)+" the entry will be skipped");
		}
	}

	private void processStateTotals(long descriptorId, DateTime currentDate, PreparedStatement descriptorInsert) throws Exception { 
		
		URL u = new URL(BASE_URL+"state&date="+URL_DATE_FORMAT.print(currentDate));
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		
		ByteArrayInputStream bais;
		if ( conn.getResponseCode() != HttpURLConnection.HTTP_OK ) {
			throw new RuntimeException("An error occurred connecting to the URL, the response code was: "+conn.getResponseCode());
		} else { // read in the full contents of the file before parsing it - this will prevent long held connections being reset
			InputStream in = conn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len; 
			while ( (len = in.read(b, 0, 1024)) > 0 ) { 
				baos.write(b, 0, len);
			}
			conn.disconnect();
			bais = new ByteArrayInputStream(baos.toByteArray());
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		String line = reader.readLine(); // chop the header

		int count = 0;
		while ( (line = reader.readLine()) != null ) {
			try {
				String[] tokens = line.split(",");
								
				descriptorInsert.setFloat(1, Float.parseFloat(tokens[3]));
				descriptorInsert.setFloat(2, Float.parseFloat(tokens[4]));
				descriptorInsert.setFloat(3, Float.parseFloat(tokens[5]));
				descriptorInsert.setFloat(4, Float.parseFloat(tokens[6]));
				descriptorInsert.setFloat(5, Float.parseFloat(tokens[7]));
				descriptorInsert.setFloat(6, Float.parseFloat(tokens[2]));
				descriptorInsert.setLong(7, descriptorId);
				descriptorInsert.setLong(8, SpatialReferenceType.US_STATE.ordinal());
				descriptorInsert.setNull(9, Types.BIGINT);
				descriptorInsert.setInt(10, USState.fromPostalCode(tokens[1]).ordinal());

				descriptorInsert.execute();
				count++;
			} catch ( SQLException sqe ) { 
				throw sqe;
			} catch ( Exception e ) { 
				LOG.error("An error occurred processing the state totals for date "+FORMATTER.print(currentDate)+" line "+count+" will be skipped");
				continue;
			}
		}
	}
	
	private void processCountyTotals(long descriptorId, DateTime currentDate, PreparedStatement descriptorInsert, 
			PreparedStatement countyQuery) throws Exception { 
		
		URL u = new URL(BASE_URL+"county&date="+URL_DATE_FORMAT.print(currentDate));
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		
		ByteArrayInputStream bais;
		if ( conn.getResponseCode() != HttpURLConnection.HTTP_OK ) {
			throw new RuntimeException("An error occurred connecting to the URL, the response code was: "+conn.getResponseCode());
		} else { // read in the full contents of the file before parsing it - this will prevent long held connections being reset
			InputStream in = conn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len; 
			while ( (len = in.read(b, 0, 1024)) > 0 ) { 
				baos.write(b, 0, len);
			}
			conn.disconnect();
			bais = new ByteArrayInputStream(baos.toByteArray());
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		String line = reader.readLine(); // chop the header

		int count = 0;
		while ( (line = reader.readLine()) != null ) {
			try {
				String[] tokens = line.split(",");
				
				descriptorInsert.setFloat(1, Float.parseFloat(tokens[5]));
				descriptorInsert.setFloat(2, Float.parseFloat(tokens[6]));
				descriptorInsert.setFloat(3, Float.parseFloat(tokens[7]));
				descriptorInsert.setFloat(4, Float.parseFloat(tokens[8]));
				descriptorInsert.setFloat(5, Float.parseFloat(tokens[9]));
				descriptorInsert.setFloat(6, Float.parseFloat(tokens[4]));
				
				descriptorInsert.setLong(7, descriptorId);
				descriptorInsert.setLong(8, SpatialReferenceType.US_COUNTY.ordinal());
				descriptorInsert.setNull(10, Types.INTEGER);

				String fips = tokens[1];
				countyQuery.setString(1, tokens[1]);
				ResultSet rs = countyQuery.executeQuery();
				if ( rs.next() ) {
					descriptorInsert.setLong(9, rs.getLong(1));
				} else { 
					throw new RuntimeException("No county was found for FIPS code "+fips+" county name "+tokens[2]+" state name "+tokens[3]);
				}
				
				descriptorInsert.execute();
				count++;
			} catch ( SQLException sqe ) { 
				throw sqe;
			} catch ( Exception e ) { 
				LOG.error("An error occurred processing the county totals for date "+FORMATTER.print(currentDate)+" line "+count+" will be skipped", e);
			}
		}
	}
	
	private long processKml(long descriptorId, long polygonId, URL kmlUrl, PreparedStatement polygonInsert, PreparedStatement pointArrayInsert) throws Exception { 
		ZipInputStream in_stream = new ZipInputStream(kmlUrl.openConnection().getInputStream());
		
		ZipEntry entry;
		do { 
			entry = in_stream.getNextEntry();
		} while ( entry != null && !entry.getName().equals("doc.kml"));
		
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in_stream);
		boolean inner = false;
		boolean poly = false;
		
		while (reader.hasNext()) {
			reader.next();
			if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
				String name = reader.getLocalName();
				if ( name.equals("Polygon") ) { 
					polygonInsert.setLong(1, polygonId);
					pointArrayInsert.setLong(3, polygonId);
					poly = true;
				} else if ( name.equals("styleUrl") ) { 
					String style = reader.getElementText();
					if ( style.equals("#PolyStyle00") ) { 
						polygonInsert.setLong(2, descriptorId);
						polygonInsert.setNull(3, Types.BIGINT);
						polygonInsert.setNull(4, Types.BIGINT);
						polygonInsert.setNull(5, Types.BIGINT);
						polygonInsert.setNull(6, Types.BIGINT);				
					} else if ( style.equals("#PolyStyle01") ) { 
						polygonInsert.setNull(2, Types.BIGINT);
						polygonInsert.setLong(3, descriptorId);
						polygonInsert.setNull(4, Types.BIGINT);
						polygonInsert.setNull(5, Types.BIGINT);
						polygonInsert.setNull(6, Types.BIGINT);	
					} else if ( style.equals("#PolyStyle02") ) { 	
						polygonInsert.setNull(2, Types.BIGINT);
						polygonInsert.setNull(3, Types.BIGINT);
						polygonInsert.setLong(4, descriptorId);
						polygonInsert.setNull(5, Types.BIGINT);
						polygonInsert.setNull(6, Types.BIGINT);	
					} else if ( style.equals("#PolyStyle03") ) { 
						polygonInsert.setNull(2, Types.BIGINT);						
						polygonInsert.setNull(3, Types.BIGINT);
						polygonInsert.setNull(4, Types.BIGINT);
						polygonInsert.setLong(5, descriptorId);						
						polygonInsert.setNull(6, Types.BIGINT);	
					} else if ( style.equals("#PolyStyle04") ) { 
						polygonInsert.setNull(2, Types.BIGINT);	
						polygonInsert.setNull(3, Types.BIGINT);
						polygonInsert.setNull(4, Types.BIGINT);
						polygonInsert.setNull(5, Types.BIGINT);
						polygonInsert.setLong(6, descriptorId);
					} else { 
						throw new Exception("The style URL could not be parsed");
					}
				} 
				
				if ( poly ) {
					if (name.equals("outerBoundaryIs")) { 
						inner = false;
					} else if (name.equals("innerBoundaryIs")) {
						inner = true;
					} else if (name.equals("coordinates")) {
						pointArrayInsert.setString(1, reader.getElementText());
						
						if ( !inner ) {
							pointArrayInsert.setInt(2, 0);
						} else { 
							pointArrayInsert.setInt(2, 1);
						}
						
						pointArrayInsert.execute();
					}
				}
			} else if (reader.getEventType() == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("Polygon") ) {
				polygonInsert.execute();			
				poly = false;
				polygonId++;
			}
		}
		
		return polygonId;
	}
	
	private DateTime createDateCounter(DateTime startDate) { 
		DateTime current_date = startDate;
		
		// round to the next Tuesday
		if ( current_date.getDayOfWeek() > DateTimeConstants.FRIDAY ) { 
			current_date = current_date.plusDays(Math.abs(DateTimeConstants.TUESDAY - current_date.getDayOfWeek()));
		} else { // round back to Tuesday of 'this' week 
			current_date = current_date.minusDays(current_date.getDayOfWeek() - DateTimeConstants.TUESDAY);
		}
		// now the start date should begin on the Tuesday closest to the provided starting date. 
		
		return current_date;
	}

    private void getDmDate(final DateTime fromDate, final DateTime toDate) {
        new Thread() {

			public void run() {

				Connection db_conn = null;

				try { 
					long start = System.currentTimeMillis();
					db_conn = source.getConnection();
					db_conn.setAutoCommit(false);

					ResultSet id_val = db_conn.createStatement().executeQuery("select max(descriptor_id) from DroughtMonitorDescriptors");
					long id = 1;
					if ( id_val.next() ) {
						id = id_val.getLong(1)+1;
					}
					id_val.close();

					long poly_id = 1;
					id_val = db_conn.createStatement().executeQuery("select max(polygon_id) from Polygons");
					if ( id_val.next() ) {
						poly_id = id_val.getLong(1)+1;
					}
					id_val.close();

					PreparedStatement insert_descriptor = db_conn.prepareStatement("insert into DroughtMonitorDescriptors (descriptor_id, map_date) values (?,?)");

					PreparedStatement insert_area = db_conn.prepareStatement("insert into DroughtMonitorAreas (d0_classified, d1_classified, " +
							"d2_classified, d3_classified, d4_classified, unclassified, descriptor, type, county, state) values (?,?,?,?,?,?,?,?,?,?)");

					PreparedStatement county_query = db_conn.prepareStatement("select county_id from Counties where fips_code = ?");

					PreparedStatement polygon_insert = db_conn.prepareStatement("insert into Polygons (polygon_id,d0,d1,d2,d3,d4) " +
							"values (?,?,?,?,?,?)");

					PreparedStatement poly_point_insert = db_conn.prepareStatement("insert into PolygonPointArrays (array_string, type, polygon) values (?,?,?)");

					DateTime current_date = createDateCounter(fromDate);
					while ( current_date.isBefore(toDate) ) {

						insert_descriptor.setLong(1, id);
						insert_descriptor.setDate(2, new java.sql.Date(current_date.getMillis()));
						insert_descriptor.execute();

						importerStatus = "Processing US totals for date "+FORMATTER.print(current_date);
						processUSTotals(id, current_date, insert_area);

						importerStatus = "Processing state totals for date "+FORMATTER.print(current_date);
						processStateTotals(id, current_date, insert_area);

						importerStatus = "Processing county totals for date "+FORMATTER.print(current_date);
						processCountyTotals(id, current_date, insert_area, county_query);

						importerStatus = "Processing map polygons for date "+FORMATTER.print(current_date);
						poly_id = processKml(id, poly_id, new URL("http://torka.unl.edu:8080/dm/data/kml/usdm"+KML_URL_FORMAT.print(current_date)+".kmz"),
								polygon_insert, poly_point_insert);

						db_conn.commit();

						current_date = current_date.plusWeeks(1);
						id++;
					}

					importerStatus = "Process complete in "+(System.currentTimeMillis() - start)+" miliseconds";
				} catch ( Exception e ) {
					LOG.error("An error occurred processing the import", e);
					importerStatus = "The importer encountered an error - "+e.getMessage();
				} finally {
					try {
						db_conn.close();
					} catch ( Exception e ) { LOG.warn("Could not close a connection" , e); }
				}
			}

		}.start();
    }
	
}
