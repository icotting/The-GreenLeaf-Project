/* Created On: May 23, 2005 */
package edu.unl.act.rma.console.acis;

import static org.omg.CORBA.TCKind.tk_double;
import static org.omg.CORBA.TCKind.tk_float;
import static org.omg.CORBA.TCKind.tk_long;
import static org.omg.CORBA.TCKind.tk_string;

import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.Any;
import org.omg.CORBA.ShortSeqHelper;
import org.omg.CORBA.TCKind;

import Meta.MetaQueryPackage.NameAnyPair;
import edu.unl.act.rma.firm.climate.WeatherStationNetwork;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.spatial.USState;


/**
 * @author Ian Cottingham
 *
 */
public class ACISResult {
	
	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ACISResult.class);

	private Map<ACISMetaField, Object> metaData;
	private ACISVariableResult availableVariables;
	private USState state;
	private WeatherStationNetwork network;
	
	protected ACISResult(NameAnyPair[] data, USState state, WeatherStationNetwork type) {
		metaData = new HashMap<ACISMetaField, Object>();
		this.state = state;
		this.network = type;
		
		for ( int i=0; i<data.length; i++ )
			try { 
				metaData.put(ACISMetaField.fromString(data[i].name), extractAnyValue(data[i].value));
			} catch ( NoSuchFieldException nfe ) { /* ignore UCAN fields that are not needed */ }
	}
	
	public void setStationID(String id) { 
		metaData.put(ACISMetaField.STATIONID, id);
	}
	
	public WeatherStationNetwork getNetwork() {
		return network;
	}

	public USState getState() {
		return state;
	}

	public Object getValue(ACISMetaField field) {
		return metaData.get(field);
	}
	
	private Object extractAnyValue(Any a) { 
		TCKind kind = a.type().kind();
		
		Object obj = null;
		if ( kind == tk_string ) { 
			obj = a.extract_string();
		} else if ( kind == tk_long ) { 
			obj = a.extract_long();
		} else if ( kind == tk_float ) { 
			obj = a.extract_float();
		} else if ( kind == tk_double ) { 
			obj = a.extract_double();
		} else if ( kind == ShortSeqHelper.type().kind() ) {
			obj = ShortSeqHelper.extract(a);
		} else { 
			LOG.info("unknown type mapping is: "+kind.value());
			LOG.warn("could not match type for "+kind.toString());
			throw new UnsupportedOperationException("could not locate type");
		}
		
		return obj;
	}
	
	protected void addAnyField(ACISMetaField field, Any val) { 
		metaData.put(field, extractAnyValue(val));
	}
	
	protected void addObjectField(ACISMetaField field, Object val) { 
		metaData.put(field, val);
	}
	
	protected void setAvailableVariables(ACISVariableResult vars) {
		availableVariables = vars;
	}
	
	public ACISVariableResult getAvailableVariables() { 		
		return availableVariables;
	}
}
