/* Created On: Jul 14, 2006 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ian Cottingham
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.METHOD)
public @interface ServicePoint {
	String description() default "";
	String defaultValue() default "";
	boolean isTransient() default false;
	boolean sendChangeNotification() default false;
}
