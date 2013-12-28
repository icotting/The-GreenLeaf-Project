/* Created On: May 24, 2005 */
package edu.unl.act.rma.console.acis;

import java.util.Collection;
import java.util.Vector;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.omg.CORBA.Any;
import org.omg.CORBA.ShortSeqHelper;

import Data.DateArrayHolder;
import Data.TSVar;
import Data.TSVarFactory;
import Meta.MetaFactory;
import Meta.MetaQuery;
import Meta.MetaFactoryPackage.MetaBusy;
import Meta.MetaQueryPackage.FieldsDate;
import Meta.MetaQueryPackage.NameAnyPair;
import Meta.MetaQueryPackage.UcanIdDate;
import edu.unl.act.rma.firm.climate.WeatherStationNetwork;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.spatial.USState;

/**
 * @author Ian Cottingham
 *
 */
public class ACISResultFactory {
	
	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ACISResultFactory.class);
	
	private static ACISResultFactory instance;
	private ACISGateway acis_interface;
	public static final float ACIS_MISSING = -99;
	
	protected DateTime lastRun; 
	protected BuildType buildType;
	
	protected ACISResultFactory(DateTime lastRun,  BuildType buildType) {  
		acis_interface = ACISGateway.getInstance();
		
		this.lastRun = lastRun;
		this.buildType = buildType;
	}
		
	public static ACISResultFactory getInstance(DateTime lastRun, BuildType buildType) { 
		if ( (instance == null) ||
				(instance.buildType != buildType) ||
				!(instance.lastRun.isEqual(lastRun)) ) {
			
			ACISGateway.reset();
			instance = new ACISResultFactory(lastRun, buildType);
		}
		return instance;
	}
	
	public static void release() { 
		instance = null;
	}
		
	public Collection<ACISResult> queryNetworkStationsByState(USState buildState, WeatherStationNetwork network) throws ACISAccessException, MetaBusy { 
		MetaFactory fac = null;
		MetaQuery query = null;
		Collection<ACISResult> vec = new Vector<ACISResult>();
		
		NameAnyPair[][] result = null;
		
		try {			
			fac = acis_interface.getMetaFactory();
			query = fac.newMetaQuery("");
			
			Any state = acis_interface.getAny();
			state.insert_string(buildState.getPostalCode());
			
			Any var = acis_interface.getAny();
			ShortSeqHelper.insert(var, ACISVariableEnumeration.getUcanIDs());
			
			NameAnyPair postal = new NameAnyPair("postal", state);
			NameAnyPair var_id = new NameAnyPair("var_major_id", var);
			NameAnyPair[] pair = { postal, var_id };
			
			String[] ids = { network.getLookupString().concat("_id") };
			
			result = query.getStnInfoAsSeq(pair, ids );
		} catch ( Exception e ) {			
			LOG.error("could not gather "+network.name()+" station info for "+buildState.name(), e);
			throw new ACISAccessException(e.getMessage()); 
		}
		
		try {
			ACISResult tmp = null;
			for ( int i=0; i<result.length; i++ ) {
				if ( (tmp = isNetworkMember(result[i], buildState, network, query)) != null ) {
					tmp.addObjectField(ACISMetaField.STATE, buildState.name());
					appendAdditionalMetaData(tmp, query);
					vec.add(tmp);
				}
			}
			
		} catch ( Exception e ) {
			LOG.error("could not gather additional "+network.name()+" station meta data for "+buildState.name());
			throw new ACISAccessException(e.getMessage());
		} finally { 
			if ( query != null )
				query.release();
			
			if ( fac != null )
				fac.release();
		}
		
		return vec;
	}
	
	public void addAvailableVariables(ACISResult result, ACISQueryWriter writer) throws ACISAccessException {
		MetaFactory fac = null;
		MetaQuery query = null;
		
		ACISVariableResult variables = new ACISVariableResult();
		try {
			fac = acis_interface.getMetaFactory();
			query = fac.newMetaQuery("");
			for ( ACISVariableEnumeration variable : ACISVariableEnumeration.values() ) {			
				FieldsDate[] fields = query.getVarInfoAsSeq("dates", ((Integer)result.getValue(ACISMetaField.ACIS_ID)), (short)variable.getUcanID(), (short)0);
				if ( fields.length != 0 ) {
					/* on an update all variables may not be added, so data should not be appened to skipped variables */
					if ( queryVariableDates(variable, variables, (Integer)result.getValue(ACISMetaField.ACIS_ID)) ) {

					}
				}
			}
			
			writer.addToTotalUnits(0-(ACISVariableEnumeration.COUNT - variables.size()));
			this.matchTemperature(variables);
			for ( ACISVariableEnumeration variable : ACISVariableEnumeration.values() ) {
				if ( !(variables.contains(variable)) )
					continue;
				
				if ( !(queryVariableData(variable, variables, (Integer)result.getValue(ACISMetaField.ACIS_ID))) )
					/* could not get data for the range, so the variable is ignored */
					variables.removeVariable(variable);
				else {
					writer.setStatus("["+result.getState().name()+" Process]: "+variable.name()+" added for station "+result.getValue(ACISMetaField.NETWORKID));
				}
				
				writer.incrementWorkedUnits(1);
			}
			
			if ( addAverageTemperature(variables) ) {
				writer.setStatus("["+result.getState().name()+" Process]: "+ACISVariableEnumeration.NORMAL_TEMP.name()+" added for station "+result.getValue(ACISMetaField.NETWORKID));
			}
			
		} catch ( Exception e ) { 
			LOG.error("error reading variable list for ACIS ID "+result.getValue(ACISMetaField.ACIS_ID), e);
			throw new ACISAccessException(e.getMessage());
		} finally { 
			if ( query != null )
				query.release();
		
			if ( fac != null )
				fac.release();
		}
		
		result.setAvailableVariables(variables);
	}
	
	private ACISResult isNetworkMember(NameAnyPair[] res, USState buildState, WeatherStationNetwork network, MetaQuery query) throws ACISAccessException { 
		UcanIdDate[] acis_ids = null;		
		ACISResult meta = new ACISResult(res, buildState, network);

		try {					
			int id = ((Integer)meta.getValue(ACISMetaField.ACIS_ID)).intValue();
			
			acis_ids = query.getIdFromUcanAsSeq(id, network.getLookupString());
			
			if ( acis_ids.length > 0 ) {
				String network_id = acis_ids[acis_ids.length - 1].id;
				
				if ( network == WeatherStationNetwork.AWDN )
					network_id = network_id.substring(1);
				
				meta.addObjectField(ACISMetaField.NETWORKID, network_id);
			} else {
				meta = null;
			}
		} catch ( Exception e ) { 
			LOG.error("error determining network membership for acis ID "+meta.getValue(ACISMetaField.ACIS_ID), e);
			throw new ACISAccessException(e.getMessage());
		} 
		
		return meta;
	}
	
	private void appendAdditionalMetaData(ACISResult res, MetaQuery query) throws ACISAccessException {		
		try {
			FieldsDate[] fields = query.getInfoForUcanIdAsSeq((Integer)res.getValue(ACISMetaField.ACIS_ID), new String[0]);
			NameAnyPair[] results = fields[fields.length - 1].fields;

			for ( int i=0; i<results.length; i++ ) {
				try { 
					res.addAnyField(ACISMetaField.fromString(results[i].name), results[i].value);
				} catch ( NoSuchFieldException nfe ) { /* ignore UCAN fields that are not needed */ } 
			}
			
		} catch ( Exception e ) {
			LOG.error("error appending additional meta data for acis ID "+res.getValue(ACISMetaField.ACIS_ID), e);
			throw new ACISAccessException(e.getMessage());
		}
	}
	
	private boolean queryVariableDates(ACISVariableEnumeration variable, ACISVariableResult variables, int acisID) throws ACISAccessException { 		
		TSVar var = null;
		try {
			var = acis_interface.getTSVarFactory().newTSVar((short)variable.getUcanID(), (short)0, acisID);
			DateArrayHolder start = new DateArrayHolder();
			DateArrayHolder end = new DateArrayHolder();
			var.getValidDateRange(start, end);
			
			short[] start_date = { start.value[0], start.value[1], start.value[2] };
			short[] end_date = { end.value[0], end.value[1], end.value[2] };
										
			variables.addVariable(variable, start_date, end_date);
			
		} catch ( Exception e ) { 
			LOG.error("error reading dates for variable "+variable.name(),e);
			throw new ACISAccessException("error reading dates for variable "+variable.name());
		} finally { 
			if ( var != null )
				var.release();
		}
		
		return true;
	}
	
	private boolean queryVariableData(ACISVariableEnumeration variable, ACISVariableResult variables, int acisID) throws ACISAccessException {
		//TODO: this enumeration type should be removed completely, NORMAL_TEMP should be stored as AVG_TEMP
		if ( variable == ACISVariableEnumeration.NORMAL_TEMP ) // this data will be added by a computation function, the UCAN ID type 3 is observed data not average
			return false;
		
		TSVar var = null;
		TSVarFactory fac = null;
		try {
			fac = acis_interface.getTSVarFactory();
			var = fac.newTSVar((short)((Integer)variable.getUcanID()).intValue(), (short)0, acisID);
			
			short[] start_date = variables.getStartDate(variable);
			short[] end_date = variables.getEndDate(variable);
			
			var.setMissingDataAsFloat(ACIS_MISSING);
			try { 
				var.setDateRange(start_date, end_date);
			} catch ( Exception e ) { 
				return false;
			} catch ( Throwable t ) { 
				LOG.error("Throwable while getting data for a variable", t);
				return false;
			}
			float[] data = convertData(var.getDataSeqAsFloat());			
			variables.addVariableData(variable, data);
			
		} catch ( Exception e ) { 
			LOG.error("error reading data for variable "+variable.name(), e);
			throw new ACISAccessException("error reading data for variable "+variable.name());
		} finally { 
			if ( var != null ) 
				var.release();
			
			if ( fac != null )
				fac.release();
		}
		
		return true;
	}
	
	private boolean addAverageTemperature(ACISVariableResult variables) { 
		float[] high = variables.getData(ACISVariableEnumeration.HIGH_TEMP);
		float[] low = variables.getData(ACISVariableEnumeration.LOW_TEMP);
		if ( high == null || low == null )
			return false;		
		
		if ( high.length != low.length ) {
			LOG.info("periods don't match, so for now the average is skipped");
			return false;
		} else { 
			float[] avg = new float[high.length];
			for ( int i=0; i<high.length; i++ ) {
				if ( (high[i] == DataType.MISSING) || (low[i] == DataType.MISSING) )
					avg[i] = DataType.MISSING;
				else 
					avg[i] = (high[i]+low[i])/2;
			}
			
			variables.addVariable(ACISVariableEnumeration.NORMAL_TEMP, variables.getStartDate(ACISVariableEnumeration.HIGH_TEMP), variables.getEndDate(ACISVariableEnumeration.HIGH_TEMP));
			variables.addVariableData(ACISVariableEnumeration.NORMAL_TEMP, avg);
			return true;
		}
		
	}
	
	/* this function ensures that the high and low temperatures have the same range */	
	private void matchTemperature(ACISVariableResult variables ) {	
		if ( !(variables.contains(ACISVariableEnumeration.HIGH_TEMP)) || !(variables.contains(ACISVariableEnumeration.LOW_TEMP)) ) 
			return;
		
		
		short[] high_start = variables.getStartDate(ACISVariableEnumeration.HIGH_TEMP);
		short[] low_start = variables.getStartDate(ACISVariableEnumeration.LOW_TEMP);
		short[] high_end = variables.getEndDate(ACISVariableEnumeration.HIGH_TEMP);
		short[] low_end = variables.getEndDate(ACISVariableEnumeration.LOW_TEMP);		
		
		DateTime high_first = new DateTime(high_start[0], high_start[1], high_start[2], 0,0,0,0, GregorianChronology.getInstance());
		DateTime low_first = new DateTime(low_start[0], low_start[1], low_start[2], 0,0,0,0, GregorianChronology.getInstance());
		DateTime high_final = new DateTime(high_end[0], high_end[1], high_end[2], 0,0,0,0, GregorianChronology.getInstance());
		DateTime low_final = new DateTime(low_end[0], low_end[1], low_end[2], 0,0,0,0, GregorianChronology.getInstance());
		
		short[] start = null;
		short[] end = null;
		if ( high_first.isAfter(low_first) )
			start = low_start;
		else
			start = high_start;
		
		
		if ( high_final.isAfter(low_final) )
			end = high_end;
		else
			end = low_end;
		
		/* replace high and low with matching ranges so that the average can be computed evenly */
		variables.addVariable(ACISVariableEnumeration.HIGH_TEMP, start, end);
		variables.addVariable(ACISVariableEnumeration.LOW_TEMP, start, end);
			
	}
	
	private float[] convertData(float[] data) { 
		float[] convert = new float[data.length];
		int pos = 0;
		for ( float f : data )
			convert[pos++] = (float)f;
		
		return convert;
	}
}
