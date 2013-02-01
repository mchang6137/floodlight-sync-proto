package sts;


/** a horribly inaccurate time source.
 *  Will give an accurate to the millisecond time. Microsecond measurements are not accurate, but hopefully consistent on a 
 *  single host (assuming the return values of System.nanoTime() are consistent within a single host
 **/
public class HorribleMicroSecondTimeSource implements MicrosecondTimeSource {
	protected static SimpleLogger log = SimpleLogger
			.getLogger(HorribleMicroSecondTimeSource.class);

	private long startTimeInMillis;
	private long startNanoAdjusted;
	
	public static final class MicroSecondTimeSourceHolder {
		static HorribleMicroSecondTimeSource instance = new HorribleMicroSecondTimeSource();
	}
	
	public static MicrosecondTimeSource getInstance() {
		return MicroSecondTimeSourceHolder.instance;
	}
	
	public HorribleMicroSecondTimeSource() {
		horribleInit();
	}
	

	private final static long NANO_TO_MILLI = 1000000L;
	private static final long NANO_TO_MICRO = 1000L;
	private static final long MICRO_TO_MILLI = 1000L;
	private static final long MILLI_TO_SEC = 1000L;
	
	// MAXIMUM number of attempts to measure a milli-second accurate start value of 
	// nanoTime
	private static final int MAX_ATTEMPTS = 59;

	/** get a (horribly) inacurated microsecond time in the system. Hopefully consistent on a single host */
	public MicroSecondTime getMicroSecondTime() {
		long measuredNano = System.nanoTime();
		long runtimeNano = measuredNano - startNanoAdjusted;
		
		long nowMillis = startTimeInMillis + (runtimeNano / NANO_TO_MILLI);
		long nanoPastMillis = runtimeNano % NANO_TO_MILLI;

		long nowSec = nowMillis / MILLI_TO_SEC;
		// COLIN LOOK HERE 
		long nowUsec = (nowMillis % MILLI_TO_SEC) * MICRO_TO_MILLI
				+ nanoPastMillis / NANO_TO_MICRO;
		return new MicroSecondTime(nowSec, nowUsec);
	}

	/** try to calibrate nanoTime against systemTimeInMillis (to millisecond precision). Does this
	 *  by measuring timeInMillis, nanoTime, timeInMillis, hopefully within the same millisecond.
	 *  
	 *  Then sets START_TIME_MILLIS to the start time, and START_NANO_ADJUSTED to the milli-second
	 *  rounded value of nanoTime.
	 */
	private void horribleInit() {
		long startMillis = 0, endMillis = 0, nano = 0;

		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			startMillis = System.currentTimeMillis();
			nano = System.nanoTime();
			endMillis = System.currentTimeMillis();

			if (startMillis == endMillis) {
				// we managed to measure startMillis and nano in the same
				// millisecond
				break;
			}
			if (i % 10 == 0) {
				try {
					// sleep a bit so we hopefully don't get descheduled
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (endMillis != startMillis) {
			log.warn(
					"Wasn't able to measure microseconds in {} tries. Timestamps will be of by e={} ms",
					MAX_ATTEMPTS, (endMillis - startMillis) / 2);
			startMillis += (endMillis - startMillis) / 2;
		}

		startTimeInMillis = startMillis;
		// truncate to millis
		startNanoAdjusted = (nano / NANO_TO_MILLI) * NANO_TO_MILLI;
	}
}
