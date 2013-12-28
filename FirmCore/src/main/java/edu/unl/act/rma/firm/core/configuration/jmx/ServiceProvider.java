/* Created On: Jul 18, 2006 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.rmi.Remote;

/**
 * @author Ian Cottingham
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
public @interface ServiceProvider {
	Class<? extends Remote> providerInterface();
}
