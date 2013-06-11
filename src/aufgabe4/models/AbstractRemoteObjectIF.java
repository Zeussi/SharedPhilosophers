package aufgabe4.models;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AbstractRemoteObjectIF extends Remote {

	public int getObjectID() throws RemoteException;
	
	/**
	 * Methods used for synchronization. 
	 * @return true if the lock could be acquired, false if otherwise.
	 * @throws RemoteException
	 */
	public boolean tryLock() throws RemoteException;
	/**
	 * @Warning unlock() could also be called by another object than the one that called lock() in first place.
	 * @Warning You should just not do that.
	 * @throws RemoteException
	 */
	public void unlock() throws RemoteException;
	
	public RemoteObjectType getObjectType() throws RemoteException;
}
