package aufgabe4.master;

import java.rmi.AlreadyBoundException;
import aufgabe4.models.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import aufgabe4.PhilosopherWorker.PhilWorkerRMIInterface;

/**
 * This program maintains a list of all computers in the network which work with philosophers.
 * It will also provide the Java RMI registry.
 * 
 * This class is a singleton.
 * 
 *
 */
public class Master implements MasterRMIInterface {
	
	private static Master masterInstance;
	
	private Registry registry;
	/**
	 * We keep the worker network addresses to lookup their remote stubs (PhilWorkerRMIInterface) with the address.
	 */
	private final ArrayList<String> workerAddresses = new ArrayList<String>();
	/**
	 * key: AbstractRemoteObject: fork/seat/phil
	 * value: worker address
	 */
	private final HashMap<AbstractRemoteObject, String> allObjects = new HashMap<AbstractRemoteObject, String>();
	// The table is represented by a list that contains objectIDs of RemoteSeat objects.
	private ArrayList<Integer> table;

	private Master() throws RemoteException {
		registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
	}
	
	public static Master getMasterInstace() {
		if (masterInstance == null) {
			try {
				masterInstance = new Master();
			} catch (RemoteException e) {
				masterInstance = null;
				e.printStackTrace();
			}
		}
		return masterInstance;
	}

	/**
	 * Here we set up the RMI stuff: registry and the MasterRMIInterface.
	 * This method also sets up the timer which checks the reachability of the workers from time to time
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final Master master = Master.getMasterInstace();
		if (master == null)
			System.exit(-1);
		
		MasterRMIInterface masterStub;
		try {
			masterStub = (MasterRMIInterface) UnicastRemoteObject.exportObject(master, 0);
			master.registry.bind("master", masterStub);
			System.out.println("Master successfully registered.");
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (AlreadyBoundException e) {
			// this cannot happen
			e.printStackTrace();
		}
		/*
		//schedule the reachability check every 2 minutes
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				master.checkWorkers();
			}
		}, 2*60*1000);
		*/
	}
	
	/**
	 * This method goes through all workers and checks their reachability
	 */
	public void checkWorkers() {
		ArrayList<String> nonreachableWorkers = new ArrayList<String>();
		
		synchronized(workerAddresses) {
			for (final String currentAddress : workerAddresses) {
				boolean workerReachable;
				try {
					PhilWorkerRMIInterface workerStub = this.getWorkerWithAddress(currentAddress);
					workerReachable = workerStub.isReachable();
				} catch (Exception e) {
					workerReachable = false;
					System.err.println("Worker with address " + currentAddress +" not reachable anymore.");
				}
				if (!workerReachable)
					nonreachableWorkers.add(currentAddress);
			}
		}
		
		System.out.println(nonreachableWorkers.size() == 0 ? "All workers are reachable from the Master." : ("Found " + nonreachableWorkers.size() + " nonreachable workers."));
		
		//unregister every worker that is not reachable anymore
		for (final String currentAddress : workerAddresses) {
			this.workerWithAddressIsNotReachable(currentAddress);
		}
	}
	
	public void workerWithAddressIsNotReachable(final String address) {
		Collection<AbstractRemoteObject> objects = this.getObjectsForWorkerWithAddress(address);
		this.removeRemoteObjects(address, objects);
		this.unregisterWorker(address);
		this.distributeObjectsToWorkers(objects);
	}
	
	public void distributeObjectsToWorkers(final Collection<AbstractRemoteObject> objects) {
		// TODO
	}
	
	public void start(final int tableSize, final int numberOfPhils) {
		// TODO: Create objects
		// TODO: build this.table array (object IDs of seat objects)
		// TODO: distribute objects to workers with this.distributeObjectsToWorkers
		// TODO: start all workers with this.startAllWorkers
	}
	
	public void restartAllWorkers() {
		this.stopAllWorkers();
		this.startAllWorkers();
	}
	
	public void startAllWorkers() {
		synchronized(workerAddresses) {
			for (final String addr : workerAddresses) {
				final PhilWorkerRMIInterface workerStub = this.getWorkerWithAddress(addr);
				try {
					workerStub.start();
				} catch (RemoteException e) {
					System.err.println("Could not start worker: " + addr);
				}
			}
		}
	}
	
	public void stopAllWorkers() {
		synchronized(workerAddresses) {
			for (final String addr : workerAddresses) {
				final PhilWorkerRMIInterface workerStub = this.getWorkerWithAddress(addr);
				try {
					workerStub.stop();
				} catch (RemoteException e) {
					System.err.println("Could not stop worker: " + addr);
				}
			}
		}
	}
	
	@Override
	public void objectWithIDIsNotReachable(final int objID) {
		if (objID < 0) {
			//just check every worker if it is reachable since we do not have the objID
			this.checkWorkers();
		}
		else {
			// assume the whole worker is not reachable anymore
			String nonreachableWorker = this.getWorkerAddressForObjectWithID(objID);
			this.workerWithAddressIsNotReachable(nonreachableWorker);
		}
	}
	
	public Collection<AbstractRemoteObject> getObjectsForWorkerWithAddress(final String address) {
		Collection<AbstractRemoteObject> retVal = new HashSet<AbstractRemoteObject>();
		synchronized(this.allObjects) {
			Set<AbstractRemoteObject> objects = this.allObjects.keySet();
			for (AbstractRemoteObject currentObject : objects) {
				String currentAddress = this.allObjects.get(currentObject);
				if (currentAddress.equalsIgnoreCase(address)) {
					retVal.add(currentObject);
				}
			}
		}
		return retVal;
	}
	
	public String getWorkerAddressForObjectWithID(final int objID) {
		String retVal = null;
		synchronized(this.allObjects) {
			Set<AbstractRemoteObject> objects = this.allObjects.keySet();
			for (AbstractRemoteObject currentObject : objects) {
				if (currentObject.getObjectID() == objID) {
					retVal = this.allObjects.get(currentObject);
					break;
				}
			}
		}
		return retVal;
	}
	
	public PhilWorkerRMIInterface getWorkerWithAddress(final String address) {
		try {
			return (PhilWorkerRMIInterface) registry.lookup(address);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * adds an object to the worker. does also maintain the records in this.allObjects
	 * @param address
	 * @param objects
	 */
	public void addRemoteObjects(final String address, final Collection<AbstractRemoteObject> objects) {
		PhilWorkerRMIInterface workerStub = this.getWorkerWithAddress(address);
		synchronized(this.allObjects) {
			for (final AbstractRemoteObject obj : objects) {
				try {
					workerStub.addRemoteObject(obj);
					this.allObjects.put(obj, address);
				} catch (RemoteException e) {
					System.err.println("Could not add object to worker. This worker may not be reachable anymore.");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * takes an object from the worker. does also maintain the records in this.allObjects
	 * @param address
	 * @param objects
	 */
	public void removeRemoteObjects(final String address, final Collection<AbstractRemoteObject> objects) {
		PhilWorkerRMIInterface workerStub = this.getWorkerWithAddress(address);
		assert(workerStub != null); //if this fails, maybe the worker is not registered anymore (unregisterWorker() has already been called)
		synchronized(this.allObjects) {
			for (final AbstractRemoteObject obj : objects) {
				try {
					this.allObjects.remove(obj);
					workerStub.takeRemoteObjectWithID(obj.getObjectID());
				} catch (RemoteException e) {
					//try to unbind the object at least from the registry hence it is not accessible by others
					try {
						registry.unbind(new Integer(obj.getObjectID()).toString());
					} catch (Exception e1) {
						// we do not need to do anything here.
					}
					System.err.println("Could not add object to worker. This worker may not be reachable anymore.");
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void registerWorker(String address) {
		synchronized(workerAddresses) {
			workerAddresses.add(address);
		}
	}

	@Override
	public void unregisterWorker(String address) {
		synchronized(workerAddresses) {
			workerAddresses.remove(address);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer> getTable() throws RemoteException {
		ArrayList<Integer> retVal;
		synchronized(this) {
			retVal = (ArrayList<Integer>) (this.table != null ? this.table.clone() : null);
		}
		return retVal;
	}
}
