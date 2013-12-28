package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;

public interface StreamFlowServiceMBean {
	
	public double getPercentComplete();
	public void createTables() throws RemoteException;
	public void buildStations(String stateExpression) throws RemoteException;
	public void loadData(String stateCode) throws RemoteException;
}
