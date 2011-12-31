package com.facebook.android;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.android.Facebook.DialogListener;

public class BaseDialogListener implements DialogListener {
	private Context context;
	private int toastTime;
	
	public BaseDialogListener(Context context, int toastTime) {
		this.context = context;
		this.toastTime = toastTime;
	}
	
	@Override
	public void onComplete(Bundle values) {
		Toast.makeText(context, android.R.string.ok, toastTime).show();
	}

	@Override
	public void onFacebookError(FacebookError e) {
		Toast.makeText(context, e.getMessage(), toastTime).show();
	}

	@Override
	public void onError(DialogError e) {
		Toast.makeText(context, e.getMessage(), toastTime).show();
		
	}

	@Override
	public void onCancel() {
		Toast.makeText(context, android.R.string.cancel, toastTime).show();
	}
}
