/* Created On: May 25, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

import pi.UcanSecure;
import pi.UcanSecureHelper;
import Broker.UCANBrokerPackage.BrokerBusy;
import Broker.UCANBrokerPackage.InvalidUserId;
import Broker.UCANBrokerPackage.NullAttribute;
import edu.unl.act.rma.console.web.NWSDataServiceManager;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class ACISGateway {
	
	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ACISGateway.class);
	
	private static ACISGateway instance = new ACISGateway();
	
	private ORB orb;
	private String ior;
	private Broker.UCANBroker ubroker;
	private String user_id;
	private UcanSecure u_secure;
	
	private ACISGateway() { 
		InputStream is = null;
		String userID = "";
		String password = "";
		
		try {
			NWSDataServiceManager manager = NWSDataServiceManager.getInstance();
			
			userID = manager.getAcisUser();
			password = manager.getAcisPassword();
			StringBuffer broker = new StringBuffer();
			broker.append("http://");
			broker.append(userID);
			broker.append(":");
			broker.append(password);
			broker.append("@");
			broker.append(manager.getBrokerIORPath());
			
	        	URL u = new URL(broker.toString());
	        	URLConnection uc = u.openConnection();
	        
			is = uc.getInputStream();
	        	byte[] b = new byte[1024];
			StringBuffer sb = new StringBuffer();
	        
	        	while ( is.read(b) > -1 )
	            sb.append(new String(b));
	               
	        this.ior = sb.toString().trim();
	        LOG.info("obtained ACIS reference "+ior);
		} catch ( MalformedURLException mfe ) { 
			LOG.error("invalid URL for IOR lookup", mfe);
		} catch ( IOException ioe ) { 
			LOG.error("error reading IOR", ioe);
		} catch ( Exception e ) { 
			LOG.error("unknown error getting IORR", e);
		} finally { 
			try {
				is.close();
			} catch ( Exception ex ) { 
				LOG.error("unknown error exiting", ex);				
			}
		}
        
		try {
	        Properties props = new Properties();
	        props.put("org.omg.PortableInterceptor.ORBInitializerClass.UcanSecureORBInitializer", ""); 	// Create and initialize the ORB
	        this.orb = ORB.init(new String[0], props);
	        
	        org.omg.CORBA.Object o = orb.string_to_object(ior);
	        
	        this.ubroker = Broker.UCANBrokerHelper.narrow(o);
	        this.user_id = ubroker.getUserTicket(userID, password);	       
	        this.u_secure = UcanSecureHelper.narrow(orb.resolve_initial_references("UcanSecure"));
	        this.u_secure.setToken(user_id);
	        
	        LOG.info("bound to ACIS");
		} catch ( BrokerBusy bb ) { 
			LOG.error("ACIS ORB broker busy", bb);
		} catch ( InvalidUserId iid ) {
			LOG.error("invalid user id for ORB access", iid);
		} catch ( NullAttribute na ) { 
			LOG.error("null attribute", na);
		} catch ( InvalidName in ) { 
			LOG.error("invalid broker name for ORB", in);
		} catch ( Exception e ) { 
			LOG.error("unknown exception", e);
		}
	}
	
	public static ACISGateway getInstance() { 		
		return instance;
	}
	
	protected static void reset() { 
		if ( instance != null )
			instance = new ACISGateway();
	}
	
    public Any getAny() { return orb.create_any(); }
    public Meta.MetaFactory getMetaFactory() throws Exception { return (Meta.MetaFactory) this.ubroker.getServer(this.user_id, "MetaFactory", ""); }
    public Data.TSVarFactory getTSVarFactory() throws Exception { return (Data.TSVarFactory) this.ubroker.getServer(this.user_id, "DailyData", ""); }
}