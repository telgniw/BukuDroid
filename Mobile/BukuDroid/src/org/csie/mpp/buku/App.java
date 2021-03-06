package org.csie.mpp.buku;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class App {
	public static final String TAG				= "BUKU";	// error log tag
	public static final int TOAST_TIME			= 5000;		// toast display time

	public static final String ISBN = "ISBN";
	
	// --- Facebook ---
	public static final String FB_APP_ID 		= "291317434239686";
	public static final String FB_APP_SECRET 	= "ff5dbf9c9a9dff31bb5cd9a00281cabd";
	
	public static final String[] FB_APP_PERMS 	= {
		"read_stream", "publish_stream"
	};
	
	public static final String FB_FAN_PAGE		= "http://www.facebook.com/pages/BukuDroid/316709318351180";
	
	public static final Facebook fb;
	public static final AsyncFacebookRunner fb_runner;
	
	// --- aNobii ---
	public static final String ANOBII_APP_KEY	= "92b7e23adf9ef1931cfd02503cf1c8ca";
	public static final String ANOBII_APP_SECRET= "kc21j1bje7";
	public static final String ANOBII_APP_SIG	= "dcad11c39d4c9ac6800715a8746c68c5";
	
	// --- Flurry ---
	public static final String FLURRY_APP_KEY	= "HLE7GZ4AE2TX4XP1LTLE";
	
	static {
		fb = new Facebook(FB_APP_ID);
		fb_runner = new AsyncFacebookRunner(fb);
	}
	
	public static enum FlurryEvent {
		TAB_ISBN,
		TAB_BARCODE,
		TAB_SEARCH,
		BOOK_NOT_FOUND,
		SHARE_ON_FB,
		FAN_PAGE_OPENED
	}
}
