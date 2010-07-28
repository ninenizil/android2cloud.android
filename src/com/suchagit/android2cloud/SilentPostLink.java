package com.suchagit.android2cloud;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;

import com.suchagit.android2cloud.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class SilentPostLink extends Activity {

	protected String SETTINGS_PREFERENCES = "android2cloud-settings";
	protected String ACCOUNTS_PREFERENCES = "android2cloud-accounts";

	protected SharedPreferences preferences;
	private static OAuthConsumer consumer;
	private static OAuthProvider provider;
	private String url;
    private PostLinkService mBoundService;
	private boolean mIsBound;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(SETTINGS_PREFERENCES, 0);
        String account = preferences.getString("account", "error");
        String host = preferences.getString("host", "error");
        String token = preferences.getString("token", "error");
        String secret = preferences.getString("secret", "error");
        if(account.equals("error") || host.equals("error") || token.equals("error") || secret.equals("error")){
        	Toast.makeText(this, "Error with your account. Please reauthenticate.", Toast.LENGTH_LONG).show();
        }
        
        String intentAction = getIntent().getAction();
        if (Intent.ACTION_SEND.equals(intentAction)) {
        	// Share
	        Bundle extras = getIntent().getExtras();
	        if (extras != null) {
	        	url = extras.getString(Intent.EXTRA_TEXT);
	        	doBindService();
	        }
        }
        finish();
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((PostLinkService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
    		mBoundService.sendLink(url, consumer, provider, preferences);
        	Toast.makeText(SilentPostLink.this, "Sent "+url+" to the cloud.", Toast.LENGTH_LONG).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(SilentPostLink.this, 
                PostLinkService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}