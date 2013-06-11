package aufgabe4.PhilosopherWorker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import aufgabe4.models.AbstractRemoteObject;

/**
 * This interface is used by the master to control the worker.
 *
 */
public interface PhilWorkerRMIInterface extends Remote {

	/**
	 * Will always return true. Obviously.
	 * @return true
	 */
	public boolean isReachable() throws RemoteException;
	
	/**
	 * This methods are used by the master to assign/remove a seat/fork/phil to/from this worker
	 * 
	 * @param fork/seat/phil
	 */
	public void addRemoteObject(final AbstractRemoteObject obj) throws RemoteException;
	public void takeRemoteObjectWithID(Integer objID) throws RemoteException;
	
	public void updateCacheForObjectIDs(final Collection<Integer> objectIDs) throws RemoteException;
	
	public void start() throws RemoteException;
	public void restart() throws RemoteException;
	public void stop() throws RemoteException;
	public boolean isRunning() throws RemoteException;
	
	/**
	 * 
	 * @return The IDs of all objects running on this worker
	 * @throws RemoteException
	 */
	public Collection<Integer> objectIDsRunningOnThisWorker() throws RemoteException;
}
