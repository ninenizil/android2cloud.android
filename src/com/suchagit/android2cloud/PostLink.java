package com.suchagit.android2cloud;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuthConsumer;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
	final int ACCOUNT_LIST_REQ_CODE = 0x1337;
	
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
	            // create a consumer object and configure it with the access
	            // token and token secret obtained from the service provider
	    		consumer = new CommonsHttpOAuthConsumer(getResources().getString(R.string.consumer_key),
	                    getResources().getString(R.string.consumer_secret));
	            consumer.setTokenWithSecret(preferences.getString("token", "error"), preferences.getString("secret", "error"));
	            // create an HTTP request to a protected resource
	            String target = preferences.getString("host", "error")+"/links/add";
	            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	    	    final EditText url_input = (EditText) findViewById(R.id.link_entry);
	    	    String url = url_input.getText().toString();
	            formparams.add(new BasicNameValuePair("link", url));
	            UrlEncodedFormEntity entity = null;
	    		try {
	    			entity = new UrlEncodedFormEntity(formparams, "UTF-8");
	    		} catch (UnsupportedEncodingException e1) {
	    			// TODO Auto-generated catch block
	    			e1.printStackTrace();
	    		}
	    		HttpPost request = new HttpPost(target);
	    		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
	            request.setEntity(entity);

	        	HttpClient client = new DefaultHttpClient();
	        	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        	
	            // sign the request
	            try {
	    			consumer.sign(request);
	    		} catch (OAuthMessageSignerException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (OAuthExpectationFailedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (OAuthCommunicationException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}

	    		String returnString = "";
	            // send the request
	            try {
	            	String response = client.execute(request, responseHandler);
	    			returnString = response;
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		Toast.makeText(PostLink.this, returnString, Toast.LENGTH_LONG).show();
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
}