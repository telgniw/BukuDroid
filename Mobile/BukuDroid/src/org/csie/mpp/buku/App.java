package org.csie.mpp.buku;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class App {
	public static final String TAG				= "BUKU";
	
	public static final String FB_APP_ID 		= "291317434239686";
	public static final String FB_APP_SECRET 	= "ff5dbf9c9a9dff31bb5cd9a00281cabd";
	
	public static final String[] FB_APP_PERMS 	= {
	};
	
	public static final Facebook fb;
	public static final AsyncFacebookRunner fb_runner;
	
	public static final String ANOBII_APP_KEY	= "92b7e23adf9ef1931cfd02503cf1c8ca";
	public static final String ANOBII_APP_SECRET= "kc21j1bje7";
	
	static {
		fb = new Facebook(FB_APP_ID);
		fb_runner = new AsyncFacebookRunner(fb);
	}
}
