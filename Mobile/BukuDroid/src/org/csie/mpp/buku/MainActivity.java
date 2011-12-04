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
import android.widget.Toast;

import com.facebook.android.SessionEvents;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionStore;
import com.facebook.android.view.FbLoginButton;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends FragmentActivity implements DialogActionListener {
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

        /* initialize ActionBar */
        actionbar = (ActionBar)findViewById(R.id.actionbar);
        actionbar.addAction(new IntentAction(this, new Intent(this, ScanActivity.class), R.drawable.star));
        
        if(!App.fb.isSessionValid())
        	actionbar.addAction(new DialogAction(this, R.layout.login, R.drawable.star, this), 0);

        /* initialize ViewPageFragments */
        viewpagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        
        bookshelf = new ViewPageFragment(getString(R.string.bookshelf), R.layout.bookshelf);
        viewpagerAdapter.addItem(bookshelf);
        
        /* initialize ViewPager */
        indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        viewpager = (ViewPager)findViewById(R.id.viewpager);

        viewpager.setAdapter(viewpagerAdapter);
        indicator.setViewPager(viewpager);

        if(App.fb.isSessionValid())
        	createSessionView();
    }
    
    private void createSessionView() {
    	stream = new ViewPageFragment(getString(R.string.stream), R.layout.stream);
		viewpagerAdapter.addItem(stream);
		
		friends = new ViewPageFragment(getString(R.string.friends), R.layout.friends);
		viewpagerAdapter.addItem(friends);
		
		viewpagerAdapter.notifyDataSetChanged();
		indicator.setCurrentItem(1);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	App.fb.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    /* --- DialogActionListener	(start) --- */
	@Override
	public void onCreate(final Dialog dialog) {
		SessionEvents.addAuthListener(new AuthListener() {
			@Override
			public void onAuthSucceed() {
				dialog.dismiss();
				createSessionView();
				actionbar.removeActionAt(0);
			}

			@Override
			public void onAuthFail(String error) {
				dialog.dismiss();
				Toast.makeText(MainActivity.this, R.string.login_failed, App.TOAST_TIME).show();
			}
		});
	}

	@Override
	public void onDisplay(final Dialog dialog) {
		FbLoginButton loginButton = (FbLoginButton)dialog.findViewById(R.id.login_button);
    	loginButton.init(this, App.fb, App.FB_APP_PERMS);
	}
	/* --- DialogActionListener	(end) --- */
}
