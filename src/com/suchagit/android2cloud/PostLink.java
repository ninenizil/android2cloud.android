package com.suchagit.android2cloud;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.suchagit.android2cloud.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PostLink extends Activity {

	protected String SETTINGS_PREFERENCES = "android2cloud-settings";
	protected String ACCOUNTS_PREFERENCES = "android2cloud-accounts";

	protected SharedPreferences preferences;
	private static OAuthConsumer consumer;
	private static OAuthProvider provider;
	final int ACCOUNT_LIST_REQ_CODE = 0x1337;

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
        setContentView(R.layout.main);
	    final Button go = (Button) findViewById(R.id.go);
	    final Button change_account = (Button) findViewById(R.id.change_account);
	    final TextView account_display = (TextView) findViewById(R.id.account_label);
        if(account.equals("error") || host.equals("error") || token.equals("error") || secret.equals("error")){
        	go.setClickable(false);
        	account_display.setText("Account: There's an error with your account. Try removing it and adding it again.");
        }else{
        	go.setClickable(true);
        	account_display.setText("Account: "+account);
        }
        
        String intentAction = getIntent().getAction();
        if (Intent.ACTION_SEND.equals(intentAction)) {
        	// Share
	        Bundle extras = getIntent().getExtras();
	        if (extras != null) {
	        	String url = extras.getString(Intent.EXTRA_TEXT);
	        	final EditText url_input = (EditText) findViewById(R.id.link_entry);
	        	url_input.setText(url);
	        }
        }
        
	    change_account.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
    			Intent i = new Intent(PostLink.this, AccountList.class);
    			startActivityForResult(i, ACCOUNT_LIST_REQ_CODE);        	
    		}
        });
	    
	    go.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		/*
	    		*/
	    		doBindService();
	    	}
	    });
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        preferences = getSharedPreferences(SETTINGS_PREFERENCES, 0);
        String account = preferences.getString("account", "error");
        String host = preferences.getString("host", "error");
        String token = preferences.getString("token", "error");
        String secret = preferences.getString("secret", "error");
	    final Button go = (Button) findViewById(R.id.go);
	    final TextView account_display = (TextView) findViewById(R.id.account_label);
        if(account.equals("error") || host.equals("error") || token.equals("error") || secret.equals("error")){
        	go.setClickable(false);
        	account_display.setText("Account: There's an error with your account. Try removing it and adding it again.");
        }else{
        	go.setClickable(true);
        	account_display.setText("Account: "+account);
        }
    }
    
    @Override
	protected void onActivityResult(int req_code, int res_code, Intent intent) {
        super.onActivityResult(req_code, res_code, intent);
        if(res_code == 1){
			SharedPreferences settings = getSharedPreferences(SETTINGS_PREFERENCES, 0);
			SharedPreferences.Editor editor = settings.edit();
			if(intent.getExtras() != null && intent.getExtras().getString("account") != null && intent.getExtras().getString("token") != null){
				String tmpAccount = intent.getExtras().getString("account");
				String[] tmpToken = intent.getExtras().getString("token").split("\\|");
				editor.putString("account", tmpAccount);
				editor.putString("host", tmpToken[2]);
				editor.putString("token", tmpToken[0]);
				editor.putString("secret", tmpToken[1]);
				Log.i("android2cloud", "tmpAccount: "+intent.getExtras().getString("account"));
				Log.i("android2cloud", "tmpToken: "+intent.getExtras().getString("token"));
				Log.i("android2cloud", "account: "+tmpAccount);
				Log.i("android2cloud", "host: "+tmpToken[2]);
				Log.i("android2cloud", "token: "+tmpToken[0]);
				Log.i("android2cloud", "secret: "+tmpToken[1]);
			}
			editor.commit();     
	        preferences = getSharedPreferences(SETTINGS_PREFERENCES, 0);
	        String account = preferences.getString("account", "error");
		    final TextView account_display = (TextView) findViewById(R.id.account_label);
	        account_display.setText("Acount: "+account);
        }
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
            Toast.makeText(PostLink.this, R.string.postlinkservice_connected,
                    Toast.LENGTH_SHORT).show();

    	    final EditText link_entry = (EditText) findViewById(R.id.link_entry);
    		String link = link_entry.getText().toString();
    		mBoundService.sendLink(link, consumer, provider, preferences);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(PostLink.this, R.string.postlinkservice_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(PostLink.this, 
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