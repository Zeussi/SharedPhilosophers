package aufgabe3.philosophers.control;

import aufgabe3.philosophers.Philosopher;

public class PhilosopherRunner extends Thread {
	
	private final Philosopher phil;
	private final PhilosopherController philController;
	
	private boolean shouldRun = true;
	

	public PhilosopherRunner(final Philosopher phil, final PhilosopherController philController) {
		super();
		assert(phil != null && philController != null && phil.getClass().equals(Philosopher.class));
		
		this.philController = philController;
		this.phil = phil;
	}
	
	
	public void run() {
		
		while (this.isAlive() && shouldRun) {
			
			if (this.phil.isHungry() && !this.phil.isWaitingForSeat()) { //try taking a seat on the table
				//actually we do not care if the philosopher did get a seat or not. we will yield anyways.
				boolean success = this.philController.seatOrQueuePhilosopher(this.phil);
				
				//this is false if the philosopher has been queued for a seat
				boolean isSeated = !this.phil.isWaitingForSeat() && success;
				
				if (isSeated)
					System.out.println("Philospher seated: " + this.phil);
				else if (success && !isSeated)
					System.out.println("Philospher queued: " + this.phil);
				else
					System.out.println("Philosopher not seated or queued: " + this.phil);
			}
			else if (this.phil.isSaturated()) { //go to the mediation room if our phil has finished eating
				this.philController.takeSeatFromPhilosopher(this.phil);
				System.out.println("Took seat from Philosopher: " + this.phil);
			}
			else { 
				//nothing to do (yield in all cases)
			}
			
			try {
				//give the cpu a bit time
				sleep(100);
			} catch (InterruptedException e) {
				// should not happen
				e.printStackTrace();
			}
			//yield();
		}
	}
	
	public void killPhilosopher() {
		this.shouldRun = false;
	}
	

	public Philosopher getPhil() {
		return phil;
	}


	
}
