/* Created On: Oct 2, 2005 */
package edu.unl.act.rma.console.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.LogManager;

/**
 * @author Ian Cottingham
 *
 */
public class IDMap implements Serializable {

	private static final long serialVersionUID = 3L;

	private static IDMap instance;
	
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, IDMap.class);
	
	private ArrayList<String> seenSet;
	private HashMap<String, Integer> seenCount;
	
	protected IDMap() { 
		seenSet = new ArrayList<String>();
		seenCount = new HashMap<String, Integer>();
	}	
	
	/* I do not use a static { } initialization here to avoid problems with trying to inject
	 * a log instance when the class is loaded, resulting in a call to the log before the logging 
	 * service has started.  Using a more "conventional" singleton pattern results in potential log
	 * calls not happening prior to the start of a logging service.
	 */
	public static IDMap getInstance() { 
		return instance == null ? instance = new IDMap() : instance;
	}
	
	public String getID(String coopId) {
		if ( seenSet.contains(coopId) ) { 
			Integer count = seenCount.get(coopId);
			if ( count == null ) { 
				count = 1;
				seenCount.put(coopId, count);
				return coopId+count;
			} else { 
				seenCount.put(coopId, ++count);
				return coopId+count;
			}
		} else { 
			seenSet.add(coopId);
			return coopId;
		}
	}
}
