package org.csie.mpp.buku.listener;

import android.content.Intent;

public interface ResultCallback {
	public void onResult(int requestCode, int resultCode, Intent data);
}
