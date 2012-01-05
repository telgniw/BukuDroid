package org.csie.mpp.buku.view;

import java.util.ArrayList;
import java.util.List;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.view.ViewPageFragment.ViewPageFragmentListener;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.LinearLayout;

public abstract class ViewManager implements ViewPageFragmentListener {
	protected final Activity activity;
	protected final DBHelper helper;
	protected final SQLiteDatabase rdb, wdb;
	
	private LinearLayout frame;
	
	private static final List<ViewManager> managers = new ArrayList<ViewManager>();
	public static void updateAll() {
		for(ViewManager man: managers) {
			if(man.viewCreated())
				man.updateView();
		}
	}
	
	public ViewManager(Activity act, DBHelper db) {
		managers.add(this);
		
		activity = act;
		helper = db;

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
