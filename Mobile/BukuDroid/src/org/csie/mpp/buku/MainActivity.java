package org.csie.mpp.buku;

import org.csie.mpp.buku.view.ViewPagerAdapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends FragmentActivity {
	protected ActionBar actionbar;
	
	protected TitlePageIndicator indicator;
	protected ViewPager viewpager;
	protected ViewPagerAdapter viewpagerAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        actionbar = (ActionBar)findViewById(R.id.actionbar);
        actionbar.addAction(new IntentAction(this, null, R.drawable.star));
        
        indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        viewpager = (ViewPager)findViewById(R.id.viewpager);
        viewpagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewpager.setAdapter(viewpagerAdapter);
        indicator.setViewPager(viewpager);
    }
}
