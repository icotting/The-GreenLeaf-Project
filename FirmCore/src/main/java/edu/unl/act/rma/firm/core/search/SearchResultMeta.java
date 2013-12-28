/* Created On Mar 27, 2007 */
package edu.unl.act.rma.firm.core.search;

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
public @interface SearchResultMeta {
	String name();
}
