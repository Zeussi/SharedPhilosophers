package aufgabe3.philosophers;

import java.util.ArrayList;
import java.util.Random;

public class Philosopher {
	
	private Seat seat;
	private int mediationTime = 500; //ms
	private int diningTime = 250; //ms
	private long timeOfLastAction = System.currentTimeMillis();
	private int philNumber;
	private long numberOfDinners = 0;
	private boolean isBlockedFromTable = false;
	//getter and setter for this var are thread-safe
	private boolean isWaitingForSeat = false;
	
	public static ArrayList<Philosopher> createPhilosophers(final int number) {
		if (number < 1) {
			System.err.println("Cannot create " + number + " Philosophers. Trouble ahead.");
			return null;
		}
		ArrayList<Philosopher> phils = new ArrayList<Philosopher>(number);
		
		for (int i = 0; i < number; i++) {
			phils.add(new Philosopher(i));
		}
		
		return phils;
	}
	
	
	public Philosopher() {
		super();
		this.philNumber = 0;
		this.randomizeMediationAndEatingTime(50, 550, 450, 950);
	}
	
	public Philosopher(int philNumber) {
		super();
		this.philNumber = philNumber;
		this.randomizeMediationAndEatingTime(50, 550, 450, 950);
	}
	

	public Seat removeFromSeat() {
		assert(this.seat != null && !this.isWaitingForSeat);
		
		final Seat oldSeat;
		
		synchronized(this) {
			oldSeat = this.seat;
			
			if (oldSeat != null) { //we may not have a seat at this time.
				this.seat.setPhil(null);
				this.seat = null;
				this.numberOfDinners++;
				
				this.timeOfLastAction = System.currentTimeMillis();
			}
		}
		
		return oldSeat;
	}
	
	public boolean assignSeat(final Seat newSeat) {
		if (this.isBlockedFromTable)
			return false;
		
		boolean success;
		
		/**
		 * Actually synchronization is not necessary here since every philosopher will have its own philosopher thread (Class PhilosopherRunner).
		 * Hence this method will only be executed from a single thread.
		 * Synchronization would only be necessary at this point if a situation could occur where a Philosopher is seated from more than one thread simultaneously.
		 */
		synchronized(this) {
			
			if (this.seat != null) { //we are already seated. we may have entered a race condition here.
				success = false;
			}
			else {
				success = newSeat.setPhil(this);
				
				if (success) {
					this.seat = newSeat;
					this.timeOfLastAction = System.currentTimeMillis();
				}
			}
		}
		
		return success;
	}
	
	public boolean queueOnSeat(final Seat newSeat) {
		if (this.isBlockedFromTable)
			return false;
		
		boolean success;
		
		/**
		 * Actually synchronization is not necessary here since every philosopher will have its own philosopher thread (Class PhilosopherRunner).
		 * Hence this method will only be executed from a single thread.
		 * Synchronization would only be necessary at this point if a situation could occur where a Philosopher is seated from more than one thread simultaneously.
		 */
		synchronized(this) {
			
			if (this.seat != null) { //we are already waiting for a seat. may be a race condition.
				success = false;
			}
			else {
				success = newSeat.queuePhil(this);
				
				if (success) {
					this.seat = newSeat;
					this.timeOfLastAction = System.currentTimeMillis();
				}
			}
		}
		
		return success;
	}
	
	public int getMediationTime() {
		return mediationTime;
	}


	public void setMediationTime(int mediationTime) {
		this.mediationTime = mediationTime;
	}


	public int getEatingTime() {
		return diningTime;
	}


	public void setEatingTime(int eatingTime) {
		this.diningTime = eatingTime;
	}
	
	public int getPhilNumber() {
		return philNumber;
	}
	

	public long getNumberOfDinners() {
		return numberOfDinners;
	}


	public void setPhilNumber(int philNumber) {
		this.philNumber = philNumber;
	}
	
	public boolean isEating() {
		return this.seat != null;
	}
	
	public boolean isMediating() {
		return this.seat == null;
	}
	
	public long numberOfDinners() {
		return this.numberOfDinners;
	}
	
	
	public boolean isBlockedFromTable() {
		return isBlockedFromTable;
	}


	public void blockFromTable() {
		boolean wasBlockedPreviously = this.isBlockedFromTable;
		this.isBlockedFromTable = true;
		
		if (!wasBlockedPreviously && this.isEating()) { //remove from seat if sitting on the table
			this.removeFromSeat();
		}
	}
	
	public void unblockFromTable() {
		this.isBlockedFromTable = false;
	}


	/**
	 * Have me mediated for to long?
	 * @return
	 */
	public boolean isHungry() {
		return this.isMediating() && this.getMediationTime() < System.currentTimeMillis() - this.timeOfLastAction;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSaturated() {
		return this.isEating() && this.timeOfLastAction < System.currentTimeMillis() - this.getEatingTime();
	}
	
	public int timeSinceLastAction() {
		return (int) (System.currentTimeMillis() - this.timeOfLastAction);
	}
	
	protected void randomizeMediationAndEatingTime(final int lowerDiningTimeBound, final int upperDiningTimeBound, final int lowerMediationTimeBound, final int upperMediationTimeBound) {
		//randomize eating time
		long range = (long)upperDiningTimeBound - (long)lowerDiningTimeBound + 1;
		Random random = new Random();
		long fraction = (long)(range * random.nextDouble());
	    int randomNumber =  (int)(fraction + lowerDiningTimeBound); 
	    this.diningTime = randomNumber;
	    
	    range = (long)upperMediationTimeBound - (long)lowerMediationTimeBound + 1;
	    random = new Random();
		fraction = (long)(range * random.nextDouble());
	    randomNumber =  (int)(fraction + lowerMediationTimeBound); 
	    this.mediationTime = randomNumber;
	}


	@Override
	public String toString() {
		return "Philosopher [seat=" + seat + ", mediationTime=" + mediationTime
				+ ", eatingTime=" + diningTime + ", philNumber=" + philNumber
				+ ", isEating()=" + isEating() + ", isHungry()=" + isHungry()
				+ ", isBlockedFromTable=" + isBlockedFromTable() 
				+ ", numberOfDinners=" + numberOfDinners()
				+ "]";
	}


	public synchronized boolean isWaitingForSeat() {
		return isWaitingForSeat;
	}


	public synchronized void setWaitingForSeat(boolean isWaitingForSeat) {
		this.isWaitingForSeat = isWaitingForSeat;
	}


	public Seat getSeat() {
		return seat;
	}
	
}
