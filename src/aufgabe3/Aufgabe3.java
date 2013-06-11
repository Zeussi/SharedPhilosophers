package aufgabe3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import aufgabe3.philosophers.Philosopher;
import aufgabe3.philosophers.Seat;
import aufgabe3.philosophers.control.PhilosopherController;

public final class Aufgabe3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numberOfPhils = 0;
		int tableSize = 0;
		int tableMasterDiningLimit = 0; // <= 0 means table master is disabled
		
		if (args.length == 2) {
		    try {
		    	numberOfPhils = Integer.parseInt(args[0]);
		    	tableSize = Integer.parseInt(args[1]);
		    } catch (NumberFormatException e) {
		        System.err.println("Arguments" + " must be integers");
		        System.exit(1);
		    }
		}
		if (args.length == 3) {
		    try {
		    	numberOfPhils = Integer.parseInt(args[0]);
		    	tableSize = Integer.parseInt(args[1]);
		    	tableMasterDiningLimit = Integer.parseInt(args[2]);
		    } catch (NumberFormatException e) {
		        System.err.println("Arguments" + " must be integers");
		        System.exit(1);
		    }
		}
		else if (args.length == 0) {
			System.out.println("Starting with default values.");
			tableSize = 5;
			numberOfPhils = 4;
		}
		else {
			System.err.println("Must either have 0 or 2 arguments. If the latter is the case, the first argument is the number of Philosophers and the second the number of seats on the table. Third argument is optional, it is the table master dining limit.\nExiting.");
			System.exit(1);
		}
		
		if (numberOfPhils < 1 || tableSize < 3) {
			System.err.println("Min number of Philosophers: 1\nMin table size: 3\nExiting.");
			System.exit(1);
		}
		
		//create stuff
		PhilosopherController philController = new PhilosopherController(Philosopher.createPhilosophers(numberOfPhils), Seat.buildTable(tableSize));
		
		//run
		System.out.println("Starting with " + numberOfPhils + " philosophers and " + tableSize + " seats on the table.\nThe TableMaster limit is " + (tableMasterDiningLimit <= 0 ? "disabled" : tableMasterDiningLimit));
		philController.startPhilosophers();
		
		if (tableMasterDiningLimit > 0)
			philController.startTableMaster(tableMasterDiningLimit);
			
		//block main thread
		System.out.println("Press enter to exit.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			br.readLine();
		} catch (IOException e) {
			System.err.println("This should not happen. What the hell have you done?");
			e.printStackTrace();
		}
		
		System.out.println("exiting...");
		philController.stopPhilosophers();
	}

}
