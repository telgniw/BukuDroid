package org.csie.mpp.buku.view;

import org.csie.mpp.buku.R;

import com.viewpagerindicator.TitleProvider;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewPagerAdapter extends FragmentPagerAdapter implements TitleProvider {
	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(final int position) {
		return new Fragment() {
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
				View v = inflater.inflate(R.layout.sample, container, false);
				TextView tv = (TextView)v.findViewById(R.id.text);
				tv.setText("Hello " + position + "!");
				return v;
			}
		};
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public String getTitle(int position) {
		return "#" + position + "#";
	}
}
