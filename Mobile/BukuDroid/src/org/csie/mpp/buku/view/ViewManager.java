package org.csie.mpp.buku.view;

import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.view.ViewPageFragment.ViewPageFragmentListener;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.FrameLayout;

public abstract class ViewManager implements ViewPageFragmentListener {
	protected final Activity activity;
	protected final DBHelper helper;
	protected final SQLiteDatabase rdb, wdb;
	
	private FrameLayout frame;
	
	public ViewManager(Activity act, DBHelper db) {
		activity = act;
		helper = db;

		rdb = helper.getReadableDatabase();
		wdb = helper.getWritableDatabase();
	}

	protected abstract int getFrameId();
	protected abstract void updateView();
	
	protected FrameLayout getFrame() {
		return frame;
	}

	/* --- ViewPageFragmentListener	(start) --- */
	@Override
	public void onCreate(View view) {
		frame = (FrameLayout)view.findViewById(getFrameId());
		updateView();
	}
	/* --- ViewPageFragmentListener	(end) --- */
}
