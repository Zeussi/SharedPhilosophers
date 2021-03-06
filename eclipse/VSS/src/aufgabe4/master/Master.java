package aufgabe4.master;

import java.rmi.AlreadyBoundException;
import aufgabe4.models.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
	
	// the time that has to pass between two remote exceptions we will actually handle
	private final int handleRemoteExceptionDelay = 5*1000;
	private long timeLastExceptionOccured = System.currentTimeMillis();
	
	
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
	private final HashMap<AbstractRemoteObject, String> distributedObjects = new HashMap<AbstractRemoteObject, String>();
	private final Set<AbstractRemoteObject> undistributedObjects = new HashSet<AbstractRemoteObject>();
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
		
		//schedule the reachability check every 1 minute
		Timer timerCheckWorker = new Timer();
		timerCheckWorker.schedule(new TimerTask() {
			@Override
			public void run() {
				master.checkWorkers();
			}
		}, 1*60*1000, 1*60*1000); // in ms - first parameter is the first timer call, second is the period timer
		
		//schedule the not distributed objects check every 5 seconds
		Timer timerCheckNotDistributedObjects = new Timer();
		timerCheckNotDistributedObjects.schedule(new TimerTask() {
			@Override
			public void run() {
				master.updateObjectDistribution();
			}
		}, 10*1000, 5*1000); // in ms - first parameter is the first timer call, second is the period timer
	}
	
	/**
	 * 
	 */
	public void updateObjectDistribution()
	{
		// distributeObjectsToWorkers(final Collection<AbstractRemoteObject> objects)
		
		// TEST DATA START - REMOVE IN RELEASE MODE
		AbstractRemoteObject obj1 = new RemotePhilosopher();
		AbstractRemoteObject obj2 = new RemotePhilosopher();
		AbstractRemoteObject obj3 = new RemotePhilosopher();
		AbstractRemoteObject obja = new RemotePhilosopher();
		AbstractRemoteObject objb = new RemotePhilosopher();
		AbstractRemoteObject objc = new RemoteFork();
		AbstractRemoteObject obje = new RemoteFork();
		AbstractRemoteObject objf = new RemoteFork();
		AbstractRemoteObject objd = new RemoteSeat(objc.getObjectID(), obje.getObjectID(), 2);
		
		
		undistributedObjects.add(obja);
		undistributedObjects.add(objb);
		undistributedObjects.add(objc);
		undistributedObjects.add(objd);
		undistributedObjects.add(obje);
		undistributedObjects.add(objf);
		distributedObjects.put(obj1,"blub1");
		distributedObjects.put(obj2,"blub1");
		distributedObjects.put(obj3,"blub3");
		
		workerAddresses.add("blub1");
		workerAddresses.add("blub2");
		workerAddresses.add("blub3");
		workerAddresses.add("blub4");
		workerAddresses.add("blub5");
		// TEST DATA END - REMOVE IN RELEASE MODE
			
		// distribute objects if new objects are available
		while(undistributedObjects.size() > 0)
		{
			String lessBusyWorker;
			
			// checking, if seat fork pairs are available, because they will be distributed to the same worker
			ArrayList<AbstractRemoteObject> seatForkPair = getSmallestSeatForkPair();
			while(seatForkPair != null)
			{
				lessBusyWorker = getLessBusyWorker(1).get(0);
				//System.out.println("Less busy worker (" + undistributedObjects.size() + "): "+ lessBusyWorker);
				
				distributeObjectsToWorkers(seatForkPair, lessBusyWorker);
				
				seatForkPair = getSmallestSeatForkPair();
			}
			
			// if no seat fork pairs are available anymore, the rest will be distributed in single way to less busy worker
			Iterator<AbstractRemoteObject> undistributedObjectsIterator = undistributedObjects.iterator();
			while(undistributedObjectsIterator.hasNext())
			{
				lessBusyWorker = getLessBusyWorker(1).get(0);
				distributeObjectsToWorkers(undistributedObjectsIterator.next(), lessBusyWorker);
			}
		}
	}
	
	/**
	 * Smallest seat-fork-pair is a seat with its left fork.
	 * If this combination is available
	 */
	private ArrayList<AbstractRemoteObject> getSmallestSeatForkPair()
	{
		ArrayList<AbstractRemoteObject> seatForkPair = null;
		Iterator<AbstractRemoteObject> undistributedObjectsIteratorForSeatSearch = undistributedObjects.iterator();
		Iterator<AbstractRemoteObject> undistributedObjectsIteratorForForkSearch = undistributedObjects.iterator();
		AbstractRemoteObject actualObjectSeat;
		AbstractRemoteObject actualObjectFork;
		boolean seatForkPairFound = false;
		
		// searching for seats
		while(undistributedObjectsIteratorForSeatSearch.hasNext()  && !seatForkPairFound)
		{
			actualObjectSeat = undistributedObjectsIteratorForSeatSearch.next();
			try
			{
				// if a seat is found ...
				if(actualObjectSeat.getObjectType() == RemoteObjectType.SEAT)
				{
					// ... find the left fork for the seat
					while(undistributedObjectsIteratorForForkSearch.hasNext() && !seatForkPairFound)
					{
						actualObjectFork = undistributedObjectsIteratorForForkSearch.next();
						// if object is a fork ...
						if(actualObjectFork.getObjectType() == RemoteObjectType.FORK
								&&  actualObjectFork.getObjectID() == getObjectWithID(((RemoteSeat) actualObjectSeat).getLeftForkID()).getObjectID()
						)
						{
							seatForkPair = new ArrayList<AbstractRemoteObject>();
							seatForkPair.add(actualObjectFork);
							seatForkPair.add(actualObjectSeat);
							seatForkPairFound = true;
						}
					}
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		return seatForkPair;
	}
	
	/**
	 * Call the less busy workers. Be careful, always ask just 1 worker (!).
	 * If you ask more than one, you don`t know if the larger one are "full".
	 * 
	 * @param numberOfWorkerNeeded The amount of worker to call. Just call 1 (!).
	 * @return The worker-address of the less busy worker.
	 */
	public ArrayList<String> getLessBusyWorker(final int numberOfWorkerNeeded)
	{
		//ArrayList<String> workerBalanace = getWorkerBalance();
		HashMap<String, Integer> numberOfObjectsByWorker = getNumberOfObjectsByWorker();
		
		// sorting the workers by the amount of objects
		HashMap<String, Integer> sortedNumberOfObjectsByWorker = sortHashMapByValuesD(numberOfObjectsByWorker);
		
		// get less busy worker(s)
		ArrayList<String> lessBusyWorker = new ArrayList<String>();
		Set<String> keys = sortedNumberOfObjectsByWorker.keySet();
		int counter = 0;
		
		for(String key : keys)
		{
			if(counter == numberOfWorkerNeeded)
				break;
			lessBusyWorker.add(key);
			counter++;
		}
		
		return lessBusyWorker;
	}
	
	/**
	 * Returns the number of objects which each worker has actually running.
	 * @return A list of worker-address and the amount of objects running on it.
	 */
	public HashMap<String, Integer> getNumberOfObjectsByWorker()
	{
		HashMap<String, Integer> numberOfObjectsByWorker = new HashMap<String, Integer>();
		
		// initialize every worker with 0 distributed objects
		for(int i = 0; i < workerAddresses.size(); i++)
			numberOfObjectsByWorker.put(workerAddresses.get(i), 0);
		
		// run through all objects, identify on which worker the object runs and count the objects of every worker
		Iterator<Entry<AbstractRemoteObject, String>> distributedObjectsIterator = distributedObjects.entrySet().iterator();
		while(distributedObjectsIterator.hasNext())
		{
		    String actualWorkerAddress = distributedObjectsIterator.next().getValue();

		    int actualObjectCounter = numberOfObjectsByWorker.get(actualWorkerAddress);
		    numberOfObjectsByWorker.put(actualWorkerAddress, ++actualObjectCounter);
		}
		
		return numberOfObjectsByWorker;
	}
	
	/* Works, but ignores duplicate 
	private HashMap<String, Integer> sortHashMap(HashMap<String, Integer> input)
	{
	    Map<String, Integer> tempMap = new HashMap<String, Integer>();
	    for(String wsState : input.keySet())
	        tempMap.put(wsState,input.get(wsState));

	    List<String> mapKeys = new ArrayList<String>(tempMap.keySet());
	    List<Integer> mapValues = new ArrayList<Integer>(tempMap.values());
	    HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	    TreeSet<Integer> sortedSet = new TreeSet<Integer>(mapValues);
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;
	    for (int i=0; i<size; i++){
	        sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), 
	                      (Integer)sortedArray[i]);
	    }
	    return sortedMap;
	}*/
	
	/**
	 * Sorts a HashMap by values. From small to large values. Duplicate values are all counted.
	 * @param passedMap The unsorted map.
	 * @return The sorted map from small to large with duplicate values.
	 */
	public LinkedHashMap<String, Integer> sortHashMapByValuesD(HashMap<String, Integer> passedMap)
	{
	    List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
	    List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	    
	    Iterator<Integer> valueIt = mapValues.iterator();
	    while (valueIt.hasNext())
	    {
	        Object val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext())
	        {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2))
	            {
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Integer)val);
	                break;
	            }
	        }
	    }
	    return sortedMap;
	}
	
	public void distributeObjectsToWorkers(final Collection<AbstractRemoteObject> objects, String workerAddress)
	{
		// TODO
		// TODO: do remote objects from undistributedObjects Set as soon as they have been distributed on a worker
	}
	
	public void distributeObjectsToWorkers(final AbstractRemoteObject object, String workerAddress)
	{
		// TODO
		// TODO: do remote objects from undistributedObjects Set as soon as they have been distributed on a worker
	}
	
	/**
	 * This method goes through all workers and checks their reachability
	 */
	public void checkWorkers() {
		//prevent this method to be executed all the time if more remotexceptions arrive from the workers
		synchronized (this) {
			if (System.currentTimeMillis() - timeLastExceptionOccured < handleRemoteExceptionDelay)
				return;
			else
				timeLastExceptionOccured = System.currentTimeMillis();
		}
		
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
		timeLastExceptionOccured = System.currentTimeMillis();
	}
	
	public void workerWithAddressIsNotReachable(final String address) {
		Collection<AbstractRemoteObject> objects = this.getObjectsForWorkerWithAddress(address);
		synchronized(undistributedObjects) {
			undistributedObjects.addAll(objects);
		}
		this.removeRemoteObjects(address, objects);
		this.unregisterWorker(address);
		this.distributeObjectsToWorkers(objects, getLessBusyWorker(1).get(0));
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
			AbstractRemoteObject deadObj = this.getObjectWithID(objID);
			synchronized(undistributedObjects) {
				undistributedObjects.add(deadObj);
			}
			this.workerWithAddressIsNotReachable(nonreachableWorker);
		}
	}
	
	@Override
	public void philosopherHasBeenKilled(final int philID) throws RemoteException {
		RemotePhilosopher killedPhil = (RemotePhilosopher) this.getObjectWithID(philID);
		String workerAddress = this.getWorkerAddressForObjectWithID(philID);
		Set<AbstractRemoteObject> philSet = new HashSet<AbstractRemoteObject>();
		philSet.add(killedPhil);
		this.removeRemoteObjects(workerAddress, philSet);
		synchronized(undistributedObjects) {
			undistributedObjects.add(killedPhil);
		}
	}

	public Collection<AbstractRemoteObject> getObjectsForWorkerWithAddress(final String address) {
		Collection<AbstractRemoteObject> retVal = new HashSet<AbstractRemoteObject>();
		synchronized(this.distributedObjects) {
			Set<AbstractRemoteObject> objects = this.distributedObjects.keySet();
			for (AbstractRemoteObject currentObject : objects) {
				String currentAddress = this.distributedObjects.get(currentObject);
				if (currentAddress.equalsIgnoreCase(address)) {
					retVal.add(currentObject);
				}
			}
		}
		return retVal;
	}
	
	public AbstractRemoteObject getObjectWithID(final int objID) {
		AbstractRemoteObject retVal = null;
		synchronized(distributedObjects) {
			Set<AbstractRemoteObject> objects = distributedObjects.keySet();
			for (AbstractRemoteObject obj : objects) {
				if (obj.getObjectID() == objID) {
					retVal = obj;
					break;
				}
			}
		}
		if (retVal == null) {
			synchronized(undistributedObjects) {
				for (AbstractRemoteObject obj : undistributedObjects) {
					if (obj.getObjectID() == objID) {
						retVal = obj;
						break;
					}
				}
			}
		}
		return retVal;
	}
	
	public String getWorkerAddressForObjectWithID(final int objID) {
		String retVal = null;
		synchronized(this.distributedObjects) {
			Set<AbstractRemoteObject> objects = this.distributedObjects.keySet();
			for (AbstractRemoteObject currentObject : objects) {
				if (currentObject.getObjectID() == objID) {
					retVal = this.distributedObjects.get(currentObject);
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
		synchronized(this.distributedObjects) {
			for (final AbstractRemoteObject obj : objects) {
				try {
					workerStub.addRemoteObject(obj);
					this.distributedObjects.put(obj, address);
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
		synchronized(this.distributedObjects) {
			for (final AbstractRemoteObject obj : objects) {
				try {
					this.distributedObjects.remove(obj);
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
	
	public static ArrayList<AbstractRemoteObject>buildTable(final int size) {
	/*	assert(size > 2);
		ArrayList<AbstractRemoteObject> table = new ArrayList<AbstractRemoteObject>(size*3);
		
		RemoteFork rightFork =  new RemoteFork();
		RemoteFork previousRightFork = rightFork;
		
		RemoteSeat previousSeat = new RemoteSeat(-1, previousRightFork.getObjectID(), 0);
		previousRightFork.setLeftSeatID(previousSeat.getObjectID());
		RemoteSeat firstSeat = previousSeat;
		RemoteSeat lastSeat;
		table.add(previousSeat);
		table.add(previousRightFork);
		
		for (int i = 0; i < size-1; i++) {
			rightFork =  new RemoteFork();
			RemoteFork leftFork = new RemoteFork();
			RemoteSeat newSeat = new RemoteSeat(previousRightFork.getObjectID(), rightFork.getObjectID(), i+1);
			
			
			
			table.add(newSeat);
			table.add(rightFork);
			
			previousRightFork = rightFork;
			lastSeat = newSeat;
		}
		
		firstSeat.setLeftFork(lastSeat.getRightFork());
		
		return table;
		*/
		return null;
	}
}
