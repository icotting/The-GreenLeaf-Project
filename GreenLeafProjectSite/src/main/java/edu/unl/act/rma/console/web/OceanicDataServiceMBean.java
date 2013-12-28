package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;

public interface OceanicDataServiceMBean {
	public void run() throws RemoteException;
	
	public double getPercentComplete();
}