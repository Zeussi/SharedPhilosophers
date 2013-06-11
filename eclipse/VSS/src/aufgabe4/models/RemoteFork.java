package aufgabe4.models;

import java.rmi.RemoteException;
import aufgabe4.PhilosopherWorker.PhilWorker;

public class RemoteFork extends AbstractRemoteObject {
	
	private int rightSeatID = -1;
	private int leftSeatID = -1;

	public RemoteFork() {
	}

	public RemoteFork(int objectID) {
		super(objectID);
	}
	
	public RemoteObjectType getObjectType() throws RemoteException {
		return RemoteObjectType.FORK;
	}
	
	public RemoteSeatIF getRightSeat() {
		if (this.rightSeatID != -1)
			return (RemoteSeatIF) PhilWorker.getPhilWorkerInstance().getObjectWithID(this.rightSeatID);
		else
			return null;
	}

	public RemoteSeatIF getLeftSeat() {
		if (this.leftSeatID != -1)
			return (RemoteSeatIF) PhilWorker.getPhilWorkerInstance().getObjectWithID(this.leftSeatID);
		else
			return null;
	}

	public void setLeftSeat(final RemoteSeatIF leftSeat) throws RemoteException {
		if (this.leftSeatID != -1) {
			RemoteSeatIF seat = null;
			seat = this.getLeftSeat();
			seat.setRightForkSimple(null);
		}
		
		leftSeat.setRightForkSimple((RemoteForkIF)this);
		this.setLeftSeatSimple(leftSeat);
	}
	
	public void setRightSeat(final RemoteSeatIF rightSeat) throws RemoteException {
		if (this.rightSeatID != -1) {
			RemoteSeatIF seat = null;
			seat = this.getRightSeat();
			seat.setLeftForkSimple(null);
		}
		
		rightSeat.setLeftForkSimple((RemoteForkIF)this);
		this.setRightSeatSimple(rightSeat);
	}
	
	public void setRightSeatSimple(final RemoteSeatIF rightSeat) throws RemoteException {
		if (rightSeat != null)
			this.rightSeatID = rightSeat.getObjectID();
		else
			this.rightSeatID = -1;
		
		//assert(this.leftSeatID != this.rightSeatID);
	}
	
	
	
	public int getRightSeatID() {
		return rightSeatID;
	}

	public void setRightSeatID(int rightSeatID) {
		this.rightSeatID = rightSeatID;
	}

	public int getLeftSeatID() {
		return leftSeatID;
	}

	public void setLeftSeatID(int leftSeatID) {
		this.leftSeatID = leftSeatID;
	}

	public void setLeftSeatSimple(final RemoteSeatIF leftSeat) throws RemoteException {
		if (leftSeat != null)
			this.leftSeatID = leftSeat.getObjectID();
		else
			this.leftSeatID = -1;
		
		//assert(this.leftSeatID != this.rightSeatID);
	}

	public boolean isTaken() throws RemoteException {
		return this.getRightSeat().isTaken() || this.getLeftSeat().isTaken();
	}
	
	public boolean isFree() throws RemoteException {
		return !this.isTaken();
	}

	@Override
	public String toString() {
		try {
			return "Fork [rightSeat=" + this.getRightSeat().getSeatNumber() + ", leftSeat=" + this.getLeftSeat().getSeatNumber()
					+ ", isTaken()=" + isTaken() + "]";
		} catch (RemoteException e) {
			PhilWorker.getPhilWorkerInstance().handleRemoteException(e, null);
			return super.toString();
		}
	}
}