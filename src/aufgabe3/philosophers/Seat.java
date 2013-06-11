package aufgabe3.philosophers;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Seat {

	private Fork rightFork;
	private Fork leftFork;
	private Philosopher phil;
	private int seatNumber;
	//the capacity specifies the max number of philosophers waiting for this seat
	private Queue<Philosopher> waitingQueue = new ArrayBlockingQueue<Philosopher>(100, true);
	
	public static ArrayList<Seat> buildTable(final int size) {
		if (size < 3) {
			System.err.println("A Table should have at least 3 seats. Trouble ahead.");
			return null;
		}
		ArrayList<Seat> table = new ArrayList<Seat>(size);
		
		
		Seat previousSeat = new Seat(new Fork(), new Fork(), 0);
		table.add(previousSeat);
		
		for (int i = 0; i < size-1; i++) {
			Seat newSeat = new Seat(previousSeat.rightFork, new Fork(), i+1);
			
			table.add(newSeat);
			
			previousSeat = newSeat;
		}
		
		assert(table.size() == size);
		
		table.get(0).setLeftFork(table.get(size-1).rightFork);
		
		return table;
	}
	
	private Seat(Fork leftFork, Fork rightFork, int number) {
		super();
		this.setRightFork(rightFork);
		this.setLeftFork(leftFork);
		this.seatNumber = number;
	}

	public boolean canBeTaken() {
		return this.rightFork.isFree() && this.leftFork.isFree();
	}
	
	public boolean isTaken() {
		return this.phil != null;
	}

	protected boolean setPhil(final Philosopher phil) {
		boolean success;
		
		/**
		 * Change order of locks for one philosopher because of... avoid deadlock problem
		 */
		synchronized((phil != null && phil.getPhilNumber() == 1) ? this.leftFork : this.rightFork) {
			synchronized ((phil != null && phil.getPhilNumber() == 1) ? this.rightFork : this.leftFork) {
				if (phil == null || this.phil == null && this.canBeTaken()) { //1st case: reset. 2nd case: this seat is not occupied and will be taken from the new philosopher
					this.phil = phil;
					success = true;
				}
				else { //this seat is already occupied. (prevent race condition)
					success = false;
				}
				
				/**
				 * Check if this seat is empty now and if we have a philosopher waiting for this seat.
				 * If true, we put the next waiting phil on the queue
				 */
				if (this.phil == null && this.canBeTaken() && !waitingQueue.isEmpty()) {
					this.phil = waitingQueue.poll();
					this.phil.setWaitingForSeat(false);
					
					System.out.println("Seat " + this.seatNumber + " became free. Will seat the next queued philosopher (" + this.phil.getPhilNumber() + ") now.");
				}
			} //synch
		} //dsynch
		
		return success;
	}
	
	/**
	 * Put a philosopher on the waiting queue. Makes only sense if this seat is currently occupied
	 * @param phil
	 * @return
	 */
	public boolean queuePhil(final Philosopher phil) {
		boolean success;
		
		phil.setWaitingForSeat(true);
		success = waitingQueue.offer(phil);
		
		return success;
	}
	
	public int getNumberOfPhilosophersWaiting() {
		return ((ArrayBlockingQueue<Philosopher>) waitingQueue).size();
	}
	
	public boolean isPhilosopherWaitingQueueFull() {
		return ((ArrayBlockingQueue<Philosopher>) waitingQueue).remainingCapacity() < 1;
	}

	public Fork getRightFork() {
		return rightFork;
	}
	
	public Fork getLeftFork() {
		return leftFork;
	}

	public void setRightFork(Fork rightFork) {
		if (this.rightFork != null)
			this.rightFork.setLeftSeatSimple(null);
		
		rightFork.setLeftSeatSimple(this);
		this.setRightForkSimple(rightFork);
	}
	
	public void setLeftFork(Fork leftFork) {
		if (this.leftFork != null)
			this.leftFork.setRightSeatSimple(null);
		
		leftFork.setRightSeatSimple(this);
		this.setLeftForkSimple(leftFork);
	}

	protected void setRightForkSimple(Fork rightFork) {
		this.rightFork = rightFork;
	}
	
	protected void setLeftForkSimple(Fork leftFork) {
		this.leftFork = leftFork;
	}
	

	public Philosopher getPhil() {
		return phil;
	}
	
	public Seat righthandSeat() {
		return this.rightFork.getRightSeat();
	}
	
	public Seat lefthandSeat() {
		return this.leftFork.getLeftSeat();
	}

	public int getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	@Override
	public String toString() {
		return "Seat [seatNumber=" + seatNumber + "]";
	}
	
}
