package blatt32;

public class Erzeuger extends AbstractNummerierterThread {

	public Erzeuger(DatenSepp datenSepp) {
		super(datenSepp);
		
	}

	public void run() {
		if(datenSepp == null) {
			System.err.println("datenSepp nicht im Fred vorhanden. Opossum installieren.");
		}
		else
		{
			while (this.isAlive()) {
				Daten input = new Daten(this.getId(), AbstractNummerierterThread.getProcessNr());
				boolean success = datenSepp.write(input);
				
				if (success) {
					System.out.println("Objekt erzeugt.");
				}
				else {
					System.out.println("�berlauf erkannt!");
					this.datenSepp.incrementWritersWaiting();
					Object monitor = this.datenSepp.writersNotifyMonitor();
					try {
						System.out.println("Erzeuger wartet: " + this.toString());
						synchronized (monitor) {
							monitor.wait();
						}
						System.out.println("Erzeuger aufgewacht: " + this.toString());
					} catch (InterruptedException e) {
						// Should not happen here
						e.printStackTrace();
					}
					this.datenSepp.decrementWritersWaiting();
				}
				
				try {
					sleep(42);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
