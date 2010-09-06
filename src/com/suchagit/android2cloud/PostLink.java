package com.suchagit.android2cloud;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;

import com.suchagit.android2cloud.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PostLink extends Activity {

	protected String SETTINGS_PREFERENCES = "android2cloud-settings";
	protected String ACCOUNTS_PREFERENCES = "android2cloud-accounts";

	protected SharedPreferences preferences;
	private static OAuthConsumer consumer;
	private static OAuthProvider provider;
	final int ACCOUNT_LIST_REQ_CODE = 0x1337;

    private PostLinkService mBoundService;
	private boolean mIsBound;
	private String url;
	boolean alerted;
	
	GoogleAnalyticsTracker tracker;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getResources().getString(R.string.analytics_ua), this);
        preferences = getSharedPreferences(SETTINGS_PREFERENCES, 0);
        String account = preferences.getString("account", "error");
		Log.i("android2cloud", "PostLink(59) account: "+account);
        String host = preferences.getString("host", "error");
		Log.i("android2cloud", "PostLink(61) host: "+host);
        String token = preferences.getString("token", "error");
		Log.i("android2cloud", "PostLink(63) token: "+token);
        String secret = preferences.getString("secret", "error");
		Log.i("android2cloud", "PostLink(65) secret: "+secret);
        Log.i("android2cloud", account);
        Log.i("android2cloud", host);
        Log.i("android2cloud", token);
        Log.i("android2cloud", secret);
        String intentAction = getIntent().getAction();
        boolean silence = preferences.getBoolean("silence", false);
		Log.i("android2cloud", "PostLink(72) silence: "+silence);
    	setContentView(R.layout.main);
    	final Button go = (Button) findViewById(R.id.go);
    	final Button change_account = (Button) findViewById(R.id.change_account);
    	final TextView account_display = (TextView) findViewById(R.id.account_label);
	    final CheckBox silence_cb = (CheckBox) findViewById(R.id.silence);
        if(!silence || !Intent.ACTION_SEND.equals(intentAction)){
            tracker.trackPageView("/PostLink");
	        silence_cb.setChecked(silence);
	        SharedPreferences accounts_prefs = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
	        Map<String, ?> accounts = accounts_prefs.getAll();
	        int size = accounts.size();
	        if(account.equals("error") && host.equals("error") && token.equals("error") && secret.equals("error")){
	        	if(size == 0) {
		        	Log.i("android2cloud", "alert");
		        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        	builder.setMessage("You don't appear to have an account setup. You need to set one up before you can use the app.")
		        		.setCancelable(false)
		        		.setPositiveButton("Let's Go", new DialogInterface.OnClickListener() {
		        			public void onClick(DialogInterface dialog, int id) {
		    	    			Intent i = new Intent(PostLink.this, AccountList.class);
		    	    			startActivityForResult(i, ACCOUNT_LIST_REQ_CODE); 
		        			}
		        		});
		        	AlertDialog alert = builder.create();
		        	alert.show();
		        	alerted = true;
	        	}else{
		        	Log.i("android2cloud", "alert");
		        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        	builder.setMessage("Your account is setup, but I'm not sure which to use. Will you tell me?")
		        		.setCancelable(false)
		        		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        			public void onClick(DialogInterface dialog, int id) {
		    	    			Intent i = new Intent(PostLink.this, AccountList.class);
		    	    			startActivityForResult(i, ACCOUNT_LIST_REQ_CODE); 
		        			}
		        		});
		        	AlertDialog alert = builder.create();
		        	alert.show();
		        	alerted = true;
	        	}
	        }
        	if(account.equals("error") || host.equals("error") || token.equals("error") || secret.equals("error")){
        		go.setClickable(false);
        		account_display.setText("Account: There's an error with your account. Try removing it and adding it again.");
        	}else{
        		go.setClickable(true);
        		account_display.setText("Account: "+account);
        	}

	        if (Intent.ACTION_SEND.equals(intentAction)) {
	        	// Share
		        Bundle extras = getIntent().getExtras();
		        if (extras != null) {
		        	final EditText url_input = (EditText) findViewById(R.id.link_entry);
		        	url = extras.getString(Intent.EXTRA_TEXT);
		        	String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		            Pattern patt = Pattern.compile(regex);
		            Matcher matcher = patt.matcher(url);
		            ArrayList<String> matches = new ArrayList<String>();
		            Log.i("android2cloud", "About to matcher.find()");
		            while(matcher.find()){
			            Log.i("android2cloud", "After matcher.find()");
		            	matches.add(matcher.group());
		            	//Toast.makeText(this, matcher.group(), Toast.LENGTH_LONG).show();
		            }
		            final CharSequence[] matches_cs = matches.toArray(new CharSequence[matches.size()]);
		            if(matches.size() > 1) {
		            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		            	builder.setTitle("Choose a link to share:");
		            	builder.setItems(matches_cs, new DialogInterface.OnClickListener() {
		            	    public void onClick(DialogInterface dialog, int item) {
		            	        url = (String) matches_cs[item];
		            	        url_input.setText(url);
		            	    }
		            	});
		            	AlertDialog alert = builder.create();
		            	alert.show();
		            }else if(matches.size() == 1){
		            	url = (String) matches_cs[0];
			        	url_input.setText(url);
		            }else{
			        	url_input.setText(url);
		            }
		        }
	        }
			Log.i("android2cloud", "PostLink(159) url: "+url);
	        
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
		    		final EditText url_input = (EditText) findViewById(R.id.link_entry);
		    		url = url_input.getText().toString();
		    		doBindService();
		    	}
		    });
		    silence_cb.setOnCheckedChangeListener(new OnCheckedChangeListener()
		    {
		        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		        {
	    			SharedPreferences.Editor editor = preferences.edit();
	    			editor.putBoolean("silence", isChecked);
	    			editor.commit();
	    			//Toast.makeText(PostLink.this, "Silent Send: "+isChecked, Toast.LENGTH_LONG).show();
		        }
		    });
        }else{
            tracker.trackPageView("/SilentPostLink");
            if(account.equals("error") || host.equals("error") || token.equals("error") || secret.equals("error")){
            	Toast.makeText(this, "Error with your account. Please check your settings in the app.", Toast.LENGTH_LONG).show();
            }
            
            if (Intent.ACTION_SEND.equals(intentAction)) {
            	// Share
    	        Bundle extras = getIntent().getExtras();
    	        if (extras != null) {
    	        	url = extras.getString(Intent.EXTRA_TEXT);
    	        	String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		            Pattern patt = Pattern.compile(regex);
		            Matcher matcher = patt.matcher(url);
		            ArrayList<String> matches = new ArrayList<String>();
		            Log.i("android2cloud", "About to matcher.find()");
		            while(matcher.find()){
			            Log.i("android2cloud", "After matcher.find()");
		            	matches.add(matcher.group());
		            	//Toast.makeText(this, matcher.group(), Toast.LENGTH_LONG).show();
		            }
		            final CharSequence[] matches_cs = matches.toArray(new CharSequence[matches.size()]);
		            if(matches.size() > 1) {
		            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		            	builder.setTitle("Choose a link to share:");
		            	builder.setItems(matches_cs, new DialogInterface.OnClickListener() {
		            	    public void onClick(DialogInterface dialog, int item) {
		            	        url = (String) matches_cs[item];
		            	        doBindService();
		                        finish();
		            	    }
		            	});
		            	AlertDialog alert = builder.create();
		            	alert.show();
		            }else if(matches.size() == 1){
		            	url = (String) matches_cs[0];
		            	doBindService();
		                finish();
		            }else{
	    	        	doBindService();
	    	            finish();
		            }
    	        }
            }
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        preferences = getSharedPreferences(SETTINGS_PREFERENCES, 0);
        String account = preferences.getString("account", "error");
		Log.i("android2cloud", "PostLink(239) account: "+account);
        String host = preferences.getString("host", "error");
		Log.i("android2cloud", "PostLink(241) host: "+host);
        String token = preferences.getString("token", "error");
		Log.i("android2cloud", "PostLink(243) token: "+token);
        String secret = preferences.getString("secret", "error");
		Log.i("android2cloud", "PostLink(245) secret: "+secret);
	    final Button go = (Button) findViewById(R.id.go);
	    final TextView account_display = (TextView) findViewById(R.id.account_label);
        SharedPreferences accounts_prefs = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
        Map<String, ?> accounts = accounts_prefs.getAll();
        int size = accounts.size();
		Log.i("android2cloud", "PostLink(251) size: "+size);
        if(account.equals("error") || host.equals("error") || token.equals("error") || secret.equals("error")){
        	go.setClickable(false);
        	account_display.setText("Account: There's an error with your account. Try removing it and adding it again.");
        }else{
        	go.setClickable(true);
        	account_display.setText("Account: "+account);
        }
        if(account.equals("error") && host.equals("error") && token.equals("error") && secret.equals("error") && !alerted){
        	if(size == 0) {
	        	Log.i("android2cloud", "alert");
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage("You don't appear to have an account setup. You need to set one up before you can use the app.")
	        		.setCancelable(false)
	        		.setPositiveButton("Let's Go", new DialogInterface.OnClickListener() {
	        			public void onClick(DialogInterface dialog, int id) {
	    	    			Intent i = new Intent(PostLink.this, AccountList.class);
	    	    			startActivityForResult(i, ACCOUNT_LIST_REQ_CODE); 
	        			}
	        		});
	        	AlertDialog alert = builder.create();
	        	alert.show();
        	}else{
	        	Log.i("android2cloud", "alert");
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage("Your account is setup, but I'm not sure which to use. Will you tell me?")
	        		.setCancelable(false)
	        		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        			public void onClick(DialogInterface dialog, int id) {
	    	    			Intent i = new Intent(PostLink.this, AccountList.class);
	    	    			startActivityForResult(i, ACCOUNT_LIST_REQ_CODE); 
	        			}
	        		});
	        	AlertDialog alert = builder.create();
	        	alert.show();
        	}
        }
        if(alerted){
        	alerted = false;
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
    		mBoundService.sendLink(url, consumer, provider, preferences);
        	//Toast.makeText(PostLink.this, "Sent "+url+" to the cloud.", Toast.LENGTH_LONG).show();
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