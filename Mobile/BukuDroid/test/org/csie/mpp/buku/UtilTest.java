package org.csie.mpp.buku;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UtilTest {
	private String[] isbn10;
	private String[] isbn13;
	
	private String upc_isbn10;
	private String upc_bar17;
	
	@Before
	public void setUp() throws Exception {
		isbn10 = new String[] {"0619217642", "9860121834"};
		isbn13 = new String[] {"9780619217648", "9789860121834"};
		
		upc_isbn10 = "0142501158";
		upc_bar17 = "05148800599550115";
	}

	@Test
	public void test() {
		for(int i = 0; i < isbn10.length && i < isbn13.length; i++) {
			System.err.println("Checking ISBN-10: " + isbn10[i]);
			assertTrue(Util.checkIsbn(isbn10[i]));
			
			System.err.println("Checking ISBN-13: " + isbn13[i]);
			assertTrue(Util.checkIsbn(isbn13[i]));
			
			System.err.println("Checking ISBN conversions ...");
			assertTrue(isbn10[i].equals(Util.toIsbn10(isbn13[i])));
			assertTrue(isbn13[i].equals(Util.toIsbn13(isbn10[i])));
			System.err.println("Ok\n");
		}

		System.err.println("Checking UPC+5 conversion...");
		System.err.println("ISBN-10: " + upc_isbn10);
		System.err.println("UPC+5: " + upc_bar17);
		assertTrue(Util.checkIsbn(upc_isbn10));
		assertFalse(Util.checkIsbn(upc_bar17));
		
		assertTrue(upc_isbn10.equals(Util.upcToIsbn(upc_bar17)));
		System.err.println("Ok\n");
	}
}
