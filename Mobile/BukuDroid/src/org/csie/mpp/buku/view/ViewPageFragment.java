package org.csie.mpp.buku.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewPageFragment extends Fragment {
	private String title;
	private int resource;
	
	private View view;
	
	public ViewPageFragment(String title, int resource) {
		this.title = title;
		this.resource = resource;
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		view = inflater.inflate(resource, container, false);
		return view;
	}
}
