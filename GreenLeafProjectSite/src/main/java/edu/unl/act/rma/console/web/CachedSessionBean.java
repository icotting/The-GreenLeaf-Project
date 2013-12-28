/* Created on: Jul 22, 2008 */
package edu.unl.act.rma.console.web;


/**
 * @author Ian Cottingham
 *
 */
public class CachedSessionBean {

	private static final long STALE_TIME = 3600000l;
	
	private final Object bean;
	private long lastCall;
	
	public CachedSessionBean(Object bean) { 
		this.bean = bean;
	}

	public void setLastCall(long lastCall) {
		this.lastCall = lastCall;
	}

	public Object getBean() {
		return bean;
	}
	
	public boolean isStale() { 
		return System.currentTimeMillis() - lastCall > STALE_TIME;
	}
}
