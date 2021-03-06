package org.csie.mpp.buku.view;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.view.ViewPageFragment.ViewPageFragmentListener;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

public abstract class ViewManager implements ViewPageFragmentListener {
	public static interface ViewListener {
		public void onListViewCreated(ListView view);
	}
	
	protected final Activity activity;
	protected final DBHelper helper;
	protected final SQLiteDatabase rdb, wdb;
	
	private LinearLayout frame;
	
	public ViewManager(Activity act, DBHelper db) {
		this(act, db, null);
	}
	
	protected final ViewListener callback;
	
	public ViewManager(Activity act, DBHelper db, ViewListener listener) {
		activity = act;
		helper = db;
		callback = listener;

		rdb = helper.getReadableDatabase();
		wdb = helper.getWritableDatabase();
	}

	protected abstract void updateView();
	
	protected LinearLayout getFrame() {
		return frame;
	}
	
	protected boolean viewCreated() {
		return frame != null;
	}

	/* --- ViewPageFragmentListener	(start) --- */
	@Override
	public void onCreate(View view) {
		frame = (LinearLayout)view.findViewById(R.id.frame);
		updateView();
	}
	/* --- ViewPageFragmentListener	(end) --- */
}
