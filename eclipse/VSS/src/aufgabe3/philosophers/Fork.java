package aufgabe3.philosophers;

public class Fork {
	
	private Seat rightSeat;
	private Seat leftSeat;

	
	public Seat getRightSeat() {
		return rightSeat;
	}

	public Seat getLeftSeat() {
		return leftSeat;
	}

	public void setLeftSeat(final Seat leftSeat) {
		if (this.leftSeat != null)
			this.leftSeat.setRightForkSimple(null);
		
		leftSeat.setRightForkSimple(this);
		this.setLeftSeatSimple(leftSeat);
	}
	
	public void setRightSeat(final Seat rightSeat) {
		if (this.rightSeat != null)
			this.rightSeat.setLeftForkSimple(null);
		
		rightSeat.setLeftForkSimple(this);
		this.setRightSeatSimple(rightSeat);
	}
	
	protected void setRightSeatSimple(final Seat rightSeat) {
		this.rightSeat = rightSeat;
		
		assert(this.leftSeat != this.rightSeat);
	}
	
	protected void setLeftSeatSimple(final Seat leftSeat) {
		this.leftSeat = leftSeat;
		
		assert(this.leftSeat != this.rightSeat);
	}

	public boolean isTaken() {
		return this.rightSeat.isTaken() || this.leftSeat.isTaken();
	}
	
	public boolean isFree() {
		return !this.isTaken();
	}

	@Override
	public String toString() {
		return "Fork [rightSeat=" + rightSeat.getSeatNumber() + ", leftSeat=" + leftSeat.getSeatNumber()
				+ ", isTaken()=" + isTaken() + "]";
	}
}
