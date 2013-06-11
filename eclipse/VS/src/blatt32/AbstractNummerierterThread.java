package blatt32;

public abstract class AbstractNummerierterThread extends Thread
{
	private static long processNr = 0;
	DatenSepp datenSepp;
	
	protected static long getProcessNr() {
		synchronized(AbstractNummerierterThread.class)
		{
			return processNr;
		}
	}

	public AbstractNummerierterThread(final DatenSepp datenSepp) {
		super();
		this.datenSepp = datenSepp;
		synchronized(AbstractNummerierterThread.class)
		{
			processNr++;
		}
	}
	
	public String toString() {
		return "Thread Nr " + this.getId();
	}
}
