package blatt32;

public class Verbraucher extends AbstractNummerierterThread {

	public Verbraucher(DatenSepp datenSepp) {
		super(datenSepp);
	}
	
	public void run() {
		if(datenSepp == null) {
			System.err.println("datenSepp nicht im Fred vorhanden. Opossum installieren.");
		}
		else
		{
			
			while (this.isAlive()) {
				Daten output = datenSepp.read();
				if(output != null)
					System.out.println("Objekt verbraucht: "+output);
				else {
					System.out.println("Keine Daten zum lesen :-( (Unterlauf)");
					this.datenSepp.incrementReadersWaiting();
					Object monitor = this.datenSepp.readersNotifyMonitor();
					try {
						System.out.println("Verbraucher wartet: " + this.toString());
						synchronized (monitor) {
							monitor.wait();
						}
						System.out.println("Verbraucher aufgewacht: " + this.toString());
					} catch (InterruptedException e) {
						// Should not happen here
						e.printStackTrace();
					}
					this.datenSepp.decrementReadersWaiting();
				}
				
				
				try {
					sleep(22);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
