package aufgabe4.models;

import java.rmi.RemoteException;



public interface RemoteForkIF extends AbstractRemoteObjectIF {

	public RemoteSeatIF getRightSeat() throws RemoteException;
	public RemoteSeatIF getLeftSeat() throws RemoteException;
	public void setLeftSeat(final RemoteSeatIF leftSeat) throws RemoteException;
	public void setRightSeat(final RemoteSeatIF rightSeat) throws RemoteException;
	public void setRightSeatSimple(final RemoteSeatIF rightSeat) throws RemoteException;
	public void setLeftSeatSimple(final RemoteSeatIF leftSeat) throws RemoteException;
	public boolean isTaken() throws RemoteException;
	public boolean isFree() throws RemoteException;
}
