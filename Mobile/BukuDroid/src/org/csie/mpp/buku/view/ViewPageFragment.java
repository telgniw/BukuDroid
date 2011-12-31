package org.csie.mpp.buku.view;

import org.csie.mpp.buku.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewPageFragment extends Fragment {
	public static interface ViewPageFragmentListener {
		void onCreate(View view);
	}
	
	protected String title;
	
	private ViewPageFragmentListener callback;
	
	public ViewPageFragment(String title) {
		this(title, null);
	}

	public ViewPageFragment(String title, ViewPageFragmentListener callback) {
		this.title = title;
		this.callback = callback;
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		View view = inflater.inflate(R.layout.pager, container, false);
		
		if(callback != null)
			callback.onCreate(view);
		
		return view;
	}
}
