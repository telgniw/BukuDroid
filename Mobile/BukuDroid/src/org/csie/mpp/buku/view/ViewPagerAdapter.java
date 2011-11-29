package org.csie.mpp.buku.view;

import com.viewpagerindicator.TitleProvider;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter implements TitleProvider {
	private ViewPageFragment[] fragments;
	
	public ViewPagerAdapter(FragmentManager fm, ViewPageFragment[] fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return fragments[position];
	}

	@Override
	public int getCount() {
		return fragments.length;
	}

	@Override
	public String getTitle(int position) {
		return fragments[position].getTitle();
	}
}
