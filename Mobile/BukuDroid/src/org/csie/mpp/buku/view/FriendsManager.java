package org.csie.mpp.buku.view;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.db.DBHelper;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class FriendsManager extends ViewManager {
	public FriendsManager(Activity activity, DBHelper helper) {
		super(activity, helper);
	}

	@Override
	protected int getFrameId() {
		return R.id.friends_frame;
	}

	@Override
	protected void updateView() {
		FrameLayout frame = getFrame();
		View view = activity.getLayoutInflater().inflate(R.layout.none, null);
		frame.addView(view);
		TextView text = (TextView)view.findViewById(R.id.inner_text);
		text.setText("You have no friends. QQ");
	}

}
