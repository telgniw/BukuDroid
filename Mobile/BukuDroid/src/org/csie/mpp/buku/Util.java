package org.csie.mpp.buku;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLConnection;

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
	
	public static String connectionToString(URLConnection conn) {
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
}
