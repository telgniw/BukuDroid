package com.facebook.android.view;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.R;
import com.facebook.android.SessionStore;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class FbLoginButton extends ImageButton implements OnClickListener, DialogListener, AuthListener, LogoutListener {
    private Facebook fb;
    private Handler handler;
    private String[] perms;
    private Activity activity;
    
    public FbLoginButton(Context context) {
        super(context);
    }
    
    public FbLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public FbLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void init(final Activity activity, final Facebook fb) {
    	init(activity, fb, new String[] {});
    }
    
    public void init(final Activity activity, final Facebook fb, final String[] perms) {
        this.activity = activity;
        this.fb = fb;
        this.perms = perms;
        handler = new Handler();
        
        setBackgroundColor(Color.TRANSPARENT);
        setAdjustViewBounds(true);
        setImageResource(fb.isSessionValid()? R.drawable.logout_button : R.drawable.login_button);
        drawableStateChanged();
        
        SessionEvents.addAuthListener(this);
        SessionEvents.addLogoutListener(this);
        setOnClickListener(this);
    }
    
    public void onClick(View view) {
        if (fb.isSessionValid()) {
            SessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(fb);
            asyncRunner.logout(getContext(), new BaseRequestListener() {
                public void onComplete(String response, final Object state) {
                    handler.post(new Runnable() {
                        public void run() {
                            SessionEvents.onLogoutFinish();
                        }
                    });
                }
            });
        }
        else {
            fb.authorize(activity, perms, this);
        }
    }
    
    public void onComplete(Bundle values) {
        SessionEvents.onLoginSuccess();
    }

    public void onFacebookError(FacebookError error) {
        SessionEvents.onLoginError(error.getMessage());
    }
    
    public void onError(DialogError error) {
        SessionEvents.onLoginError(error.getMessage());
    }

    public void onCancel() {
        SessionEvents.onLoginError("Action Canceled");
    }
    
    public void onAuthSucceed() {
        SessionStore.save(fb, getContext());
        setImageResource(R.drawable.logout_button);
        drawableStateChanged();
    }

    public void onAuthFail(String error) {
    }
    
    public void onLogoutBegin() {           
    }
    
    public void onLogoutFinish() {
        SessionStore.clear(getContext());
        setImageResource(R.drawable.login_button);
        drawableStateChanged();
    }
    
}
