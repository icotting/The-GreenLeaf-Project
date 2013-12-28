/* Created on Nov 20, 2008 */
package edu.unl.act.rma.firm.core;

/**
 * 
 * @author Ian Cottingham
 *
 */
public interface Logger {

	public void debug(String str, Throwable t);
	public void info(String str, Throwable t);
	public void warn(String str, Throwable t);
	public void error(String str, Throwable t);
	public void fatal(String str, Throwable t);
	
	public void debug(String str);
	public void info(String str);
	public void warn(String str);
	public void error(String str);
	public void fatal(String str);
	
}
