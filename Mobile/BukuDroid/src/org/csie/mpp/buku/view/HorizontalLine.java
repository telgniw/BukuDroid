package org.csie.mpp.buku.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class HorizontalLine extends FrameLayout {
	public HorizontalLine(Context context) {
		super(context);
		init(context);
	}
	
	public HorizontalLine(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public HorizontalLine(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		View view = new View(context);
		view.setBackgroundColor(0xff999999);
		view.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, 1));
		addView(view);
	}
}
