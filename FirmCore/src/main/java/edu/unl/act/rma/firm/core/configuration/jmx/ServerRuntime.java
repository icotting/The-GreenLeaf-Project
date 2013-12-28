/* Created On: Oct 30, 2007 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 * Environment variables for the configuration service
 * 
 * edu.unl.firm.configuration.server - indicates that a new MBeanServer should be created
 * 
 * edu.unl.firm.configuration.server.user - the user ID to make remote connections to the MBeanServer, used only with 
 * edu.unl.firm.configuration.server 
 * 
 * edu.unl.firm.configuration.server.password - the password to make remote connections to the MBeanServer, used only with
 * edu.unl.firm.configuration.server 
 * 
 * edu.unl.firm.configuration.server.host - the host to run the server on, used only with
 * edu.unl.firm.configuration.server 
 * 
 * edu.unl.firm.configuration.server.port - the port to run the server on, used only with
 * edu.unl.firm.configuration.server 
 * 
 * edu.unl.firm.configuration.server.deploy.dir - the path to the jar file service deployments (NOTE: this is optional
 * and is generally used when the server runtime is not executed in a middleware container, for FIRM this is
 * not used as each manager class deals with deployment)
 * 
 * edu.unl.firm.configuration.persist.dir - the path the the persisted configuration values
 * 
 */
public class ServerRuntime {
	
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, ServerRuntime.class);
		
	protected static MBeanServer server;
	
	protected static ServerRuntime _instance;
	
	public static ServerRuntime getInstance() { 
		if ( _instance == null ) { 
			_instance = new ServerRuntime();
		}
		
		return _instance;
	}
	
	public MBeanServer getMBeanServer() { return this.server; }
		
	protected ServerRuntime() {
		try {
			if ( System.getProperty("edu.unl.firm.configuration.server") == null ) {
				server = ManagementFactory.getPlatformMBeanServer();
			} else {
				
				int serverPort;
				try {
					serverPort = Integer.parseInt(System.getProperty("edu.unl.firm.configuration.server.port"));
				} catch ( Exception e ) { 
					throw new RuntimeException("a valid value must be specified for the environment variable " +
							"edu.unl.firm.configuration.server.port");
				}
				
				String serverHost = System.getProperty("edu.unl.firm.configuration.server.host");
				if ( serverHost == null ) { 
					throw new RuntimeException("a valid value must be provided for the environment variable " +
							"edu.unl.firm.configuration.server.host");
				}
				
				LocateRegistry.createRegistry(serverPort);
				
				StringBuffer service_url = new StringBuffer("service:jmx:rmi:///jndi/rmi://");
				service_url.append(serverHost);
				service_url.append(":");
				service_url.append(serverPort);
				service_url.append("/jmxrmi");
				
				JMXServiceURL address = new JMXServiceURL(service_url.toString());

				server = MBeanServerFactory.createMBeanServer();
				Map<String, Object> env = new HashMap<String, Object>();
				
				String[] server_credentials = new String[2];
				server_credentials[0] = System.getProperty("edu.unl.firm.configuration.server.user");
				server_credentials[1] = System.getProperty("edu.unl.firm.configuration.server.password");
				
				env.put("jmx.remote.credentials", server_credentials);
				
				JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(address, env, server);
				cntorServer.start();
				LOG.info("The service container is running at: "+cntorServer.getAddress());
			}
		} catch ( Exception e ) { 
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void deploy(Class clazz) { 
		ServiceDeployer.deploy(clazz);
	}
	
	public void deploy(Object object) { 
		ServiceDeployer.deploy(object);
	}
	
	public void undeployServices() { 
		ServiceDeployer.undeployAll();
	}
	
	public boolean isDeployed(Class clazz) { 
		return ServiceDeployer.isDeployed(clazz);
	}
	
	/**
	 * This method should only be called when configuration objects are 1) running in another container and 2)
	 * do not deal with deploying themselves.  If the server runtime is being deployed as part of a Java EE 
	 * application, it is best to use the ServiceDeployer to deploy the objects when they are loaded.
	 */
	public void BootStrapJmxServer() {
		String service_path = "/tmp";
		if ( System.getProperty("edu.unl.firm.configuration.server.deploy.dir") == null ) { 
			LOG.warn("The service path was not provided, the default path will be used.");
		} else { 
			service_path = System.getProperty("edu.unl.firm.configuration.server.deploy.dir");
		}
		
		try {
			final File dir = new File(service_path);
			if ( !dir.exists() ) {
				LOG.error("The service path does not exist, the service container will be terminated.");
				System.exit(-1);
			}

			new Thread() { 
				public void run() {
					ArrayList<String> deployed_files = new ArrayList<String>();
					
					while ( true ) {
						try {
							ArrayList<URL> jar_files = new ArrayList<URL>();
							
							for ( File f : dir.listFiles() ) {
								if ( deployed_files.contains(f.getName()) ) {
									continue;
								}
								deployed_files.add(f.getName());
								
								if ( f.getName().endsWith(".jar") ) {
									jar_files.add(f.toURL());
								}
							}
							boolean verbose = ( System.getProperty("FIRM_VERBOSE") == null ) ? false : true;
							
							URLClassLoader loader = new URLClassLoader(jar_files.toArray(new URL[jar_files.size()]));
							
							for ( URL url : jar_files ) { 
								JarFile jar = new JarFile(new File(url.toString().substring(5)));
								for ( Enumeration e = jar.entries(); e.hasMoreElements(); ) {
									JarEntry entry = (JarEntry)e.nextElement();
									if ( entry.getName().endsWith(".class") ) {
										String class_name = entry.getName().substring(0, entry.getName().indexOf(".class"));
										class_name = class_name.replaceAll("/", ".");
		
										Class clazz = loader.loadClass(class_name);
										if ( clazz.getAnnotation(Service.class) != null ) {
											if ( verbose ) {
												LOG.info("Deploying service class: "+class_name);
											}
											ServiceDeployer.deploy(clazz);
										}
									}
								}
							}
						} catch ( Exception e ) { 
							LOG.error("An error occured while loading a service", e);
						}
						
						try {
							Thread.sleep(2000);
						} catch ( Exception e ) { /* do nothing as this indicates some serious JVM issues */ }
					}
				}
			}.start();

		} catch ( Exception ioe ) { 
			LOG.error("An error occured while setting up the service deployer", ioe);
			RuntimeException re = new RuntimeException();
			re.initCause(ioe);
			throw re;
		}
	}
	
}
