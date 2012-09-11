package sts;

public class MicroSecondTime implements Comparable<MicroSecondTime>{
	public final long seconds;
	public final long microSeconds;
	
	public MicroSecondTime(long seconds, long microSeconds) {
		this.seconds = seconds;
		this.microSeconds = microSeconds;		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (microSeconds ^ (microSeconds >>> 32));
		result = prime * result + (int) (seconds ^ (seconds >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MicroSecondTime other = (MicroSecondTime) obj;
		if (microSeconds != other.microSeconds)
			return false;
		if (seconds != other.seconds)
			return false;
		return true;
	}

	@Override
	public int compareTo(MicroSecondTime o) {
		if(this.seconds < o.seconds)
			return -1;
		else if (this.seconds > o.seconds)
			return 1;
		else if (this.microSeconds < o.microSeconds)
			return -1;
		else if (this.microSeconds > o.microSeconds)
			return 1;
		else 
			return 0;
	}
	
	public String toString() {
		return String.format("%d.%06d", seconds, microSeconds);
	}
}
