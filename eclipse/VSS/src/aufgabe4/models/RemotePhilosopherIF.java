package aufgabe4.models;

import java.rmi.RemoteException;

public interface RemotePhilosopherIF extends AbstractRemoteObjectIF {
	public RemoteSeatIF removeFromSeat() throws RemoteException;
	public boolean assignSeat(final RemoteSeatIF newSeat) throws RemoteException;
	public boolean queueOnSeat(final RemoteSeatIF newSeat) throws RemoteException;
	public int getMediationTime() throws RemoteException;
	public void setMediationTime(int mediationTime) throws RemoteException;
	public int getEatingTime() throws RemoteException;
	public void setEatingTime(int eatingTime) throws RemoteException;
	public int getPhilNumber() throws RemoteException;
	public long getNumberOfDinners() throws RemoteException;
	public void setPhilNumber(int philNumber) throws RemoteException;
	public boolean isEating() throws RemoteException;
	public boolean isMediating() throws RemoteException;
	public long numberOfDinners() throws RemoteException;
	public boolean isBlockedFromTable() throws RemoteException;
	public void blockFromTable() throws RemoteException;
	public void unblockFromTable() throws RemoteException;
	public boolean isHungry() throws RemoteException;
	public boolean isSaturated() throws RemoteException;
	public int timeSinceLastAction() throws RemoteException;
	public boolean isWaitingForSeat() throws RemoteException;
	public void setWaitingForSeat(boolean isWaitingForSeat);
	public RemoteSeatIF getSeat() throws RemoteException;
}