package blatt32;

import java.util.LinkedList;

public class DatenSepp {
	private LinkedList<Daten> list = new LinkedList<Daten>();
	private final int listCapacity = 100;
	
	/**
	 * These two are used to count readers and writers waiting because of an underflow or overflow condition that occured.
	 * These vars are also used as monitor to use wait / notify synchronization.
	 */
	private int readersWaiting = 0;
	private int writersWaiting = 0;
	
	private final Object readersMonitor = new Object();
	private final Object writersMonitor = new Object();
	
	public Daten read()
	{
		synchronized(list)
		{
			if(list.isEmpty()) //underflow!
				return null;
			Daten data = list.removeLast();
			
			//success - check if there are writers waiting and wake the next one
			//System.out.println(this.writersWaiting + " writers waiting");
			if (this.writersWaiting()) {
				synchronized(this.writersNotifyMonitor()) {
					this.writersNotifyMonitor().notify();
				}
			}
			
			return data;
		}
	}
	
	public boolean write(Daten data)
	{
		boolean success;
		synchronized(list)
		{
			if (this.listCapacity > this.list.size()) {
				list.add(data);
				success = true;
				
				//System.out.println(this.readersWaiting + " readers waiting");
				if (this.readersWaiting()) {
					synchronized(this.readersNotifyMonitor()) {
						this.readersNotifyMonitor().notify();
					}
				}
				
			}
			else { //overflow!
				success = false;
			}
		}
		return success;
	}
	
	public synchronized void incrementReadersWaiting() {
		this.readersWaiting++;
	}
	
	public synchronized void incrementWritersWaiting() {
		this.writersWaiting++;
	}
	
	public synchronized void decrementReadersWaiting() {
		this.readersWaiting--;
	}
	
	public synchronized void decrementWritersWaiting() {
		this.writersWaiting--;
	}
	
	public synchronized boolean readersWaiting() {
		assert(this.readersWaiting >= 0);
		return this.readersWaiting != 0;
	}
	
	public synchronized boolean writersWaiting() {
		assert(this.writersWaiting >= 0);
		return this.writersWaiting != 0;
	}
	
	public Object readersNotifyMonitor() {
		return this.readersMonitor;
	}
	
	public Object writersNotifyMonitor() {
		return this.writersMonitor;
	}
}
