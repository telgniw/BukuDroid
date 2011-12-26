package org.csie.mpp.buku;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringEscapeUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Util {
	public static String isbn13To10(String isbn13) {
		StringBuilder builder = new StringBuilder();
		builder.append(isbn13.substring(3,12));
		int sum = 0;
		for(int i = 0; i < 9; ++i) {
			sum += (isbn13.charAt(i + 3) - '0') * (10 - i);
		}
		int m = sum % 11;
		if(m == 1)
			builder.append('X');
		else if(m == 0)
			builder.append(0);
		else
			builder.append(11 - m);
		return builder.toString();
	}
	
	public static String isbn10To13(String isbn10) {
		StringBuilder builder = new StringBuilder();
		builder.append("978").append(isbn10.substring(0, 9));
		int sum = 0;
		for(int i = 0; i < 9; i++) {
			sum += (isbn10.charAt(i + 3) - '0') * (((i & 1) == 0)? 1 : 3);
		}
		int m = sum % 10;
		builder.append(m);
		return builder.toString();
	}
	
	public static String upcToIsbn(String upcPlus5) {
		StringBuilder builder = new StringBuilder();
		String manufacturer = upcPlus5.substring(0, 6);
		// TODO: map manufacturer to ISBN publisher code (http://www.librarything.com/wiki/index.php/UPC)
		String publisher = manufacturer.substring(0, 4);
		builder.append(publisher);
		builder.append(upcPlus5.subSequence(12, 17));
		String isbn9 = builder.toString();
		return isbn10To13(isbn9);
	}
	
	public static String urlToString(URL url) {
		StringBuilder builder = new StringBuilder();
		try {
			URLConnection conn = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		catch (Exception e) {
			Log.e(App.TAG, e.toString());
		}
		return builder.toString();
	}
	
	public static Bitmap urlToImage(URL url) {
		try {
			URLConnection conn = url.openConnection();
			InputStream is = new BufferedInputStream(conn.getInputStream());
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			is.close();
			return bitmap;
		}
		catch(Exception e) {
			Log.e(App.TAG, e.toString());
		}
		return null;
	}
	
	public static String htmlToText(String str) {
		str = str.replaceAll("<{1}[^>]{1,}>{1}", "");
		return StringEscapeUtils.unescapeHtml4(str);
	}
	
	public static byte[] toByteArray(Bitmap bitmap) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
			return os.toByteArray();
		}
		catch(Exception e) {
			Log.e(App.TAG, e.toString());
		}
		return null;
	}
}
