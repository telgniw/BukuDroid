package org.csie.mpp.buku;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UtilTest {
	private String isbn10;
	private String isbn13;
	
	private String upc_isbn10;
	private String upc_bar17;
	
	@Before
	public void setUp() throws Exception {
		isbn10 = "0619217642";
		isbn13 = "9780619217648";
		
		upc_isbn10 = "0142501158";
		upc_bar17 = "0514880599550115";
	}

	@Test
	public void test() {
		assertTrue(Util.checkIsbn(isbn10));
		assertTrue(Util.checkIsbn(isbn13));
		
		assertTrue(Util.checkIsbn(upc_isbn10));
		assertFalse(Util.checkIsbn(upc_bar17));
		
		assertTrue(isbn10.equals(Util.toIsbn10(isbn13)));
		assertTrue(isbn13.equals(Util.toIsbn13(isbn10)));
		
		assertTrue(upc_isbn10.equals(Util.upcToIsbn(upc_bar17)));
	}
}
