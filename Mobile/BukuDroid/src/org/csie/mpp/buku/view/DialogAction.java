package org.csie.mpp.buku.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

public class DialogAction implements Action {
	public interface DialogActionListener {
		void onCreate(Dialog dialog);
		void onDisplay(Dialog dialog);
	}
	
	private Activity activity;
	private int layout;
	private int drawable;
	private DialogActionListener callback;
	
	private Dialog dialog;
	
	public DialogAction(Activity activity, int layout, int drawable) {
		this(activity, layout, drawable, null);
	}
	
	public DialogAction(Activity activity, int layout, int drawable, DialogActionListener callback) {
		this.activity = activity;
		this.layout = layout;
		this.drawable = drawable;
		this.callback = callback;
	}
	
	@Override
	public int getDrawable() {
		return drawable;
	}
	
	@Override
	public void performAction(View view) {
		if(dialog == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			dialog = new AlertDialog.Builder(activity).setView(
				inflater.inflate(layout, null)
			).create();
			
			if(callback != null)
				callback.onCreate(dialog);
		}
		
		dialog.show();
		
		if(callback != null)
			callback.onDisplay(dialog);
	}
}
