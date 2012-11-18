package sts;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HorribleMicroSecondTimeSourceTest {
	@Test
	public void basicTest() {
		HorribleMicroSecondTimeSource s = new HorribleMicroSecondTimeSource();
		
		MicroSecondTime t = s.getMicroSecondTime();
		assertTrue(t.seconds > 0);
		assertTrue(Math.abs(System.currentTimeMillis()/1000 - t.seconds) < 1);
		
		MicroSecondTime t2 = s.getMicroSecondTime();
		assertTrue(t2.seconds >= t.seconds);
		assertTrue(t2.seconds > t.seconds || t2.microSeconds > t.microSeconds);
		assertTrue(t2.compareTo(t) > 0);
		System.out.println("delta" + (t2.microSeconds - t.microSeconds));
	}

	@Test
	public void testTwoSources() throws InterruptedException {
		HorribleMicroSecondTimeSource s1 = new HorribleMicroSecondTimeSource();
		Thread.sleep(10);
		HorribleMicroSecondTimeSource s2 = new HorribleMicroSecondTimeSource();
		
		MicroSecondTime t1_from_s1 = s1.getMicroSecondTime();
		for(int i=1;i < 50; i++);
		MicroSecondTime t2_from_s2 = s2.getMicroSecondTime();
		for(int i=1;i < 50; i++);
		MicroSecondTime t3_from_s2 = s2.getMicroSecondTime();
		for(int i=1;i < 50; i++);
		MicroSecondTime t4_from_s1 = s1.getMicroSecondTime();
		
		assertTrue(t2_from_s2.compareTo(t1_from_s1) > 0);
		assertTrue(t3_from_s2.compareTo(t2_from_s2) > 0);
		assertTrue("T4 from s1 ("+t4_from_s1+") should be bigger than T3 from S2("+t3_from_s2+")", t4_from_s1.compareTo(t3_from_s2) > 0);
	}
}
