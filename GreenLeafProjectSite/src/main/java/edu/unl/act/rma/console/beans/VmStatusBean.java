/* Created on: Jan 27, 2010 */
package edu.unl.act.rma.console.beans;

import java.text.SimpleDateFormat;

import javax.inject.Named;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

/**
 * @author Ian Cottingham
 *
 */
@Named("vmBean")
public class VmStatusBean {
	private static final SimpleDateFormat D_FORMATTER = new SimpleDateFormat();
	
	static { 
		D_FORMATTER.applyPattern("MMM dd, yyyy hh:mm:ss a");
	}
	
	private Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, VmStatusBean.class);
	private ConfigurationServer classLoaderInfo;
	private ConfigurationServer osInfo;
	private ConfigurationServer runtimeInfo;
	private ConfigurationServer threadInfo;
	private ConfigurationServer memoryInfo;
	
	public VmStatusBean() { 
		try { 
			osInfo = new ConfigurationServer(new ObjectName("java.lang:type=OperatingSystem"));
			runtimeInfo = new ConfigurationServer(new ObjectName("java.lang:type=Runtime"));
			classLoaderInfo = new ConfigurationServer(new ObjectName("java.lang:type=ClassLoading"));
			threadInfo = new ConfigurationServer(new ObjectName("java.lang:type=Threading"));		
			memoryInfo = new ConfigurationServer(new ObjectName("java.lang:type=Memory"));				
		} catch ( Exception e ) { 
			LOG.error("could not connect to the configuration servers", e);
			RuntimeException re = new RuntimeException("could not load the vm info bean");
			re.initCause(e);
			throw re;
		}
	}
	
	public String getArchitecture() { 
		return (String)getValue(osInfo, "Arch");
	}
	
	public int getProcessorCount() { 
		return (Integer)getValue(osInfo, "AvailableProcessors");
	}
	
	public long getVirtualMemory() { 
		Long val = (Long)getValue(osInfo, "CommittedVirtualMemorySize");
		return val /1048567;
	}
	
	public long getFreePhysicalMem() { 
		Long val = (Long)getValue(osInfo, "FreePhysicalMemorySize");
		return val / 1048567;
	}
	
	public long getTotalPhysicalMem() { 
		Long val = (Long)getValue(osInfo, "TotalPhysicalMemorySize");
		return val / 1048567;
	}
	
	public String getOs() { 
		return (String)getValue(osInfo, "Name");
	}
	
	public long getCpuTime() { 
		Long val = (Long)getValue(osInfo, "ProcessCpuTime");
		return val / (6000*60);
	}
	
	public String getVmName() { 
		return (String)getValue(runtimeInfo, "VmName");
	}
	
	public String getVmVendor() { 
		return (String)getValue(runtimeInfo, "VmVendor");
	}
	
	public String getVmVersion() { 
		return (String)getValue(runtimeInfo, "VmVersion");
	}
	
	public String getStartTime() { 
		Long date = (Long)getValue(runtimeInfo, "StartTime");
		return D_FORMATTER.format(date);
	}
	
	public long getUpTime() { 
		Long val = (Long)getValue(runtimeInfo, "Uptime");
		return val / ((60000*60)*24);
	}
	
	public int getLoadedClasses() { 
		return (Integer)getValue(classLoaderInfo, "LoadedClassCount");
	}
	
	public long getTotalLoadedClasses() { 
		return (Long)getValue(classLoaderInfo, "TotalLoadedClassCount");
	}
	
	public long getUnloadedClasses() { 
		return (Long)getValue(classLoaderInfo, "UnloadedClassCount");
	}
	
	public long getThreadCpuTime() { 
		Long val = (Long)getValue(threadInfo, "CurrentThreadCpuTime");
		return val / 6000*60;
	}
	
	public long getThreadUserCpuTime() { 
		Long val = (Long)getValue(threadInfo, "CurrentThreadUserTime");
		return val / 6000*60;
	}
	
	public int getDaemonThreadCount() { 
		return (Integer)getValue(threadInfo, "DaemonThreadCount");
	}
	
	public int getPeakThreadCount() { 
		return (Integer)getValue(threadInfo, "PeakThreadCount");
	}
	
	public int getThreadCount() { 
		return (Integer)getValue(threadInfo, "ThreadCount");
	}
	
	public long getTotalStartedThreadCount() { 
		return (Long)getValue(threadInfo, "TotalStartedThreadCount");
	}
	
	public long getHeapCommitted() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "HeapMemoryUsage", "committed");
		return val / 1024;
	}
	
	public long getHeapInit() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "HeapMemoryUsage", "init");
		return val / 1024;		
	}
	
	public long getHeapMax() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "HeapMemoryUsage", "max");
		return val / 1024;
	}
	
	public long getHeapUsed() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "HeapMemoryUsage", "used");
		return val / 1024;		
	}
	
	public long getNonHeapCommitted() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "NonHeapMemoryUsage", "committed");
		return val / 1024;		
	}
	
	public long getNonHeapInit() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "NonHeapMemoryUsage", "init");
		return val / 1024;		
	}
	
	public long getNonHeapMax() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "NonHeapMemoryUsage", "max");
		return val / 1024;		
	}
	
	public long getNonHeapUsed() { 
		Long val = (Long)getValueFromComposite(memoryInfo, "NonHeapMemoryUsage", "used");
		return val / 1024;		
	}
	
	private Object getValue(ConfigurationServer server, String attribute) { 
		try {
			return server.get(attribute);
		} catch ( ConfigurationException ce ) { 
			LOG.error("An error occured getting attribute "+attribute, ce);
			return "Error Result";
		}
	}
	
	private Object getValueFromComposite(ConfigurationServer server, String attribute, String comp) { 
		try {
			CompositeDataSupport support = (CompositeDataSupport)server.get(attribute);
			return support.get(comp);
		} catch ( Exception e ) { 
			LOG.error("An error occured getting attribute "+attribute, e);
			return "Error Result";
		}
	}
}
