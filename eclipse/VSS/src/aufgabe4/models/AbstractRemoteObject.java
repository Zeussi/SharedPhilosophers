package aufgabe4.models;

import java.rmi.RemoteException;


public abstract class AbstractRemoteObject implements AbstractRemoteObjectIF {
	
	/**
	 * object ID has to be unique amount all objects
	 */
	private final int objectID;
	private static int nextObjectID = 0;
	private boolean lock = false;

	public AbstractRemoteObject() {
		this.objectID = AbstractRemoteObject.generateNextObjectID();
	}
	
	public AbstractRemoteObject(int objectID) {
		this.objectID = objectID;
	}
	
	@Override
	public int getObjectID() {
		return objectID;
	}
	
	@Override
	public int hashCode() {
		return this.getObjectID();
	}
	
	@Override
	public boolean tryLock() throws RemoteException {
		boolean success;
		synchronized(this) {
			if (!lock) {
				lock = true;
				success = true;
			}
			else
				success = false;
		}
		return success;
	}

	@Override
	public void unlock() throws RemoteException {
		synchronized(this) {
			lock = false;
		}
	}

	private static int generateNextObjectID() {
		int retVal;
		synchronized(AbstractRemoteObject.class) {
			retVal = nextObjectID;
			nextObjectID++;
		}
		return retVal;
	}
}
