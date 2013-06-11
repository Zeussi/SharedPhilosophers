package aufgabe4.models;

import java.rmi.RemoteException;


public interface RemoteSeatIF extends AbstractRemoteObjectIF {

	public boolean canBeTaken() throws RemoteException;
	public boolean isTaken() throws RemoteException;
	public boolean setPhil(final RemotePhilosopherIF phil) throws RemoteException;
	public boolean queuePhil(final RemotePhilosopherIF phil) throws RemoteException;
	public int getNumberOfPhilosophersWaiting() throws RemoteException;
	public boolean isPhilosopherWaitingQueueFull() throws RemoteException;
	public RemotePhilosopherIF getNextPhilosopherWaiting() throws RemoteException;
	public RemoteForkIF getRightFork() throws RemoteException;
	public RemoteForkIF getLeftFork() throws RemoteException;
	public void setRightFork(RemoteForkIF rightFork) throws RemoteException;
	public void setLeftFork(RemoteForkIF leftFork) throws RemoteException;
	public void setRightForkSimple(RemoteForkIF rightFork) throws RemoteException;
	public void setLeftForkSimple(RemoteForkIF leftFork) throws RemoteException;
	public RemotePhilosopherIF getPhil() throws RemoteException;
	public RemoteSeatIF righthandSeat() throws RemoteException;
	public RemoteSeatIF lefthandSeat() throws RemoteException;
	public int getSeatNumber() throws RemoteException;
	public void setSeatNumber(int seatNumber) throws RemoteException;
}
