package org.csie.mpp.buku.view;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.db.DBHelper;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendsManager extends ViewManager {
	public FriendsManager(Activity activity, DBHelper helper) {
		super(activity, helper);
	}

	@Override
	protected void updateView() {
		LinearLayout frame = getFrame();
		TextView text = (TextView)frame.findViewById(R.id.text);
		text.setText("You have no friends. QQ");
	}

}
