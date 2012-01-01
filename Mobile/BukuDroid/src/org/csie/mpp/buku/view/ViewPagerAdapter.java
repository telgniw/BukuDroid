package org.csie.mpp.buku.view;

import java.util.ArrayList;
import java.util.List;

import com.viewpagerindicator.TitleProvider;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter implements TitleProvider {
	private List<ViewPageFragment> fragments;
	
	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fragments = new ArrayList<ViewPageFragment>();
	}
	
	public void addItem(ViewPageFragment fragment) {
		fragments.add(fragment);
	}
	
	public void removeItem(ViewPageFragment fragment) {
		fragments.remove(fragment);
	}
	
	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public String getTitle(int position) {
		return fragments.get(position).getTitle();
	}
}
