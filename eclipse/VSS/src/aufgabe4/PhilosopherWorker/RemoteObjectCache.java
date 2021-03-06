package aufgabe4.PhilosopherWorker;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import aufgabe4.models.AbstractRemoteObjectIF;

public class RemoteObjectCache {
	
	private final HashMap<Integer, AbstractRemoteObjectIF> cache = new HashMap<Integer, AbstractRemoteObjectIF>();

	public RemoteObjectCache() {}
	
	public AbstractRemoteObjectIF getRemoteObjectWithID(final int objID) {
		AbstractRemoteObjectIF retVal;
		synchronized(cache) {
			retVal = cache.get(objID);
		}
		return retVal;
	}

	public void saveObjectToCache(final AbstractRemoteObjectIF obj) throws RemoteException {
		synchronized(cache) {
			cache.put(obj.getObjectID(), obj);
		}
	}
	
	public void removeObjectIDsFromCache(final Collection<Integer> objectIDs) {
		synchronized(cache) {
			for (Integer objID : objectIDs)
				cache.remove(objID);
		}
	}
}
