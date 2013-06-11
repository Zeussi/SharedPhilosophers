package aufgabe3.philosophers.control;

import java.util.HashSet;

import aufgabe3.philosophers.Philosopher;

public class TableMaster extends Thread {
	
	private final PhilosopherController philController;
	private boolean shouldRun = true;
	private int maxDiningDifference = 10;

	public TableMaster(final PhilosopherController philC) {
		super();
		this.philController = philC;
	}
	
	public void run() {
		
		HashSet<Philosopher> phils = this.philController.getPhilosopers();
		
		if (phils.size() < 2) {
			System.err.println("TableMaster should not be run with less than 2 Philosophers.");
			shouldRun = false;
		}
		
		while(this.isAlive() && shouldRun) {
			long avgDinings = 0;
			
			//calculate the average number of dinings
			
			for (Philosopher phil : phils)
				avgDinings += phil.getNumberOfDinners();
			
			avgDinings /= phils.size();
			
			System.out.println("TableMaster: Average number of dinners amoung all Philosophers: " + avgDinings);
			
			
			//check if a phil differs from the avg
			//caution: this method is not very accurate
			
			
			/**
			 * How this works:
			 * We check if a philosopher is dining more than the average. If yes, we block it from the table by setting a flag within the Philosopher class.
			 * The Philosopher is unblocked if its number of dinners go way below the average of dinners.
			 * If a Philosopher is continuously below the average with its number of dinners, we undertake no action.
			 */
			for (Philosopher phil : phils) {
				long delta = phil.numberOfDinners() - avgDinings;
				boolean isOverfeed = delta > 0; //ate to much
				delta = delta < 0 ? delta*-1 : delta; //get value
				
				if (delta > this.maxDiningDifference && isOverfeed) { //phil ate to much
					if (!phil.isBlockedFromTable()) { //block the philosopher from the table
						phil.blockFromTable();
						System.out.println(phil + " blocked from table.");
						
						//do not block more than one philosopher per run. we may end up in a blocking situation otherwise.
						break;
					}
				}
				else if (delta > this.maxDiningDifference && !isOverfeed) { //phil ate to less
					if (phil.isBlockedFromTable()) { //unblock if it is blocked
						phil.unblockFromTable();
						System.out.println(phil + " unblocked from table.");
					}
					else
						System.out.println("TableMaster found a Philosopher which is almost starving. There is no mechanism implemented to compensate for such situation.");
				}
			}
			
			//wait until we check next
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("This should not happen.\nBurning and dying.");
				e.printStackTrace();
			}
		}
	}
	
	
	public void suicide() {
		this.shouldRun = false;
	}

	public int getMaxDiningDifference() {
		return maxDiningDifference;
	}

	public void setMaxDiningDifference(int maxDiningDifference) {
		this.maxDiningDifference = maxDiningDifference;
	}
	
	
}
