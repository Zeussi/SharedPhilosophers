package blatt32;

public class Daten {
	private long processId;
	private long processNr;
	
	public Daten(long processId, long processNr)
	{
		this.processId = processId;
		this.processNr = processNr;
	}

	public long getProcessId() {
		return processId;
	}

	public long getProcessNr() {
		return processNr;
	}

	@Override
	public String toString() {
		return "Daten [processId=" + processId + ", processNr=" + processNr
				+ "]";
	}
}
