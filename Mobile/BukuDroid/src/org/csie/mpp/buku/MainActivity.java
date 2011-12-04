package org.csie.mpp.buku;

import org.csie.mpp.buku.view.DialogAction;
import org.csie.mpp.buku.view.DialogAction.DialogActionListener;
import org.csie.mpp.buku.view.ViewPageFragment;
import org.csie.mpp.buku.view.ViewPagerAdapter;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ListView;

import com.facebook.android.SessionEvents;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.facebook.android.SessionStore;
import com.facebook.android.view.FbLoginButton;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends FragmentActivity implements AuthListener, LogoutListener {
	protected ActionBar actionbar;
	
	protected TitlePageIndicator indicator;
	protected ViewPager viewpager;
	protected ViewPagerAdapter viewpagerAdapter;
	
	protected ViewPageFragment bookshelf, stream, friends;
	protected ListView booklist;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* initialize FB */
        SessionStore.restore(App.fb, this);
        SessionEvents.addAuthListener(this);
        SessionEvents.addLogoutListener(this);

        /* initialize ActionBar */
        actionbar = (ActionBar)findViewById(R.id.actionbar);
        actionbar.addAction(new IntentAction(this, new Intent(this, ScanActivity.class), R.drawable.star));
        
        if(App.fb.isSessionValid() == false) {
        	actionbar.addAction(new DialogAction(this, R.layout.login, R.drawable.star, new DialogActionListener() {
				@Override
				public void onCreate(final Dialog dialog) {
					SessionEvents.addAuthListener(new AuthListener() {
						@Override
						public void onAuthSucceed() {
							dialog.dismiss();
						}

						@Override
						public void onAuthFail(String error) {	
						}
					});
				}
				
				@Override
				public void onDisplay(final Dialog dialog) {
		        	FbLoginButton loginButton = (FbLoginButton)dialog.findViewById(R.id.login_button);
		        	loginButton.init(MainActivity.this, App.fb, App.FB_APP_PERMS);
				}
        	}), 0);
        }

        /* initialize ViewPageFragments */
        viewpagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        
        bookshelf = new ViewPageFragment(getString(R.string.bookshelf), R.layout.bookshelf);
        viewpagerAdapter.addItem(bookshelf);
        
    	if(App.fb.isSessionValid())
    		createSessionView();
        
        /* initialize ViewPager */
        indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        viewpager = (ViewPager)findViewById(R.id.viewpager);

        viewpager.setAdapter(viewpagerAdapter);
        indicator.setViewPager(viewpager);

        if(App.fb.isSessionValid())
        	indicator.setCurrentItem(1);
    }
    
    private void createSessionView() {
    	stream = new ViewPageFragment(getString(R.string.stream), R.layout.stream);
		viewpagerAdapter.addItem(stream);
		
		friends = new ViewPageFragment(getString(R.string.friends), R.layout.friends);
		viewpagerAdapter.addItem(friends);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	App.fb.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    /* --- AuthListener		(start) --- */
	@Override
	public void onAuthSucceed() {
		createSessionView();
		actionbar.removeActionAt(0);
		viewpagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onAuthFail(String error) {
	}
	/* --- AuthListener		(end) --- */

	/* --- LogoutListener	(start) --- */
	@Override
	public void onLogoutBegin() {
	}

	@Override
	public void onLogoutFinish() {
	}
	/* --- LogoutListener	(end) --- */
}
