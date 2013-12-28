/* Created On: Jun 3, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * A class to indicate the state of a task.  The states listed are informational only
 * and are the responsibility of the implementing class to set.  These values do not
 * directly reflect the thread state of the Task in question.
 * 
 * @author Ian Cottingham
 */
public enum TaskState {

	/** indicates that the current task is running */
	RUNNING(),
	
	/** 
	 * indicates that the current task is waiting on results, this state should
	 * be set on any calls to the get() method of the Future<V> result of the 
	 * excecuting task 
	 */
	WAITING(),
	
	/** indicates the the current task exited with an exception */
	ERROR(), 
	
	/** the default state for newly created tasks */
	NEVERRUN(), 
	
	/** indicates that the process was intentionally stopped **/
	HALTED(),
	
	/** indicates that the task has completed and had no errors */
	COMPLETE() { };

	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
