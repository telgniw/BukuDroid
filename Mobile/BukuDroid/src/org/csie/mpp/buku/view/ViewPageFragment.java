package org.csie.mpp.buku.view;

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
	protected int resource;
	
	private ViewPageFragmentListener callback;
	
	public ViewPageFragment(String title, int resource) {
		this(title, resource, null);
	}

	public ViewPageFragment(String title, int resource, ViewPageFragmentListener callback) {
		this.title = title;
		this.resource = resource;
		this.callback = callback;
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		View view = inflater.inflate(resource, container, false);
		
		if(callback != null)
			callback.onCreate(view);
		
		return view;
	}
}
