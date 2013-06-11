package blatt32;

public class Verbraucher extends AbstractNummerierterThread {

	public Verbraucher(DatenSepp datenSepp) {
		super(datenSepp);
	}
	
	public void run() {
		if(datenSepp == null) {
			System.err.println("datenSepp nicht im Fred vorhanden. Opossum installieren.");
			System.exit(-1);
		}
		else
		{
			
			while (this.isAlive()) {
				Daten output = datenSepp.read();
				if(output != null)
					System.out.println("Objekt verbraucht: "+output);
				else {
					try {
						System.out.println("Keine Daten zum lesen :-( (Unterlauf) Thread " + this.toString() + " wartet (wait)");
						synchronized (this.datenSepp.readersNotifyMonitor()) {
							this.datenSepp.incrementReadersWaiting();
							this.datenSepp.readersNotifyMonitor().wait();
							this.datenSepp.decrementReadersWaiting();
						}
						System.out.println("Thread " + this.toString() + " aufgewacht (notify)");
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
				
				
				try {
					sleep(22);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}
}
