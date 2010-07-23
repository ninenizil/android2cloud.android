package com.suchagit.android2cloud;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
//import android.widget.Toast;

public class OAuth extends Activity {
	private static OAuthConsumer consumer;
	private static OAuthProvider provider;
	private static String host;
	private static String account;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		host = "";
		account = "";
		if(this.getIntent() != null && this.getIntent().getExtras() != null && this.getIntent().getExtras().getString("host") != null){
			host = this.getIntent().getExtras().getString("host");
		}
		if(this.getIntent() != null && this.getIntent().getExtras() != null && this.getIntent().getExtras().getString("account") != null){
			account = this.getIntent().getExtras().getString("account");
		}
		//Toast.makeText(this, host, Toast.LENGTH_LONG).show();
		String oauth_request_url = getRequestURL(host, getResources());
		//Toast.makeText(this, oauth_request_url, Toast.LENGTH_LONG).show();
		WebView browser= new WebView(this);
		setContentView(browser);

		browser.getSettings().setJavaScriptEnabled(true);

		final Activity activity = this;
		browser.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			// Activities and WebViews measure progress with different scales.
			// The progress meter will automatically disappear when we reach 100%
			activity.setProgress(progress * 1000);
			}
		});
		browser.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url){
				super.onPageFinished(view, url);
				String callback = host+getResources().getString(R.string.callback_url);
				Log.i("android2cloud", url+"|"+callback);
				if(url.length() >= callback.length() && url.substring(0, callback.length()).equals(callback)){
					//Toast.makeText(OAuth.this, "Positive: "+url, Toast.LENGTH_LONG).show();
					Log.i("android2cloud", url);
					Intent intent = new Intent(OAuth.this, AccountAdd.class);
					intent.putExtra("host", host);
					String verifier = "";
					String[] params = url.split("&");
					for(String param:params){
						if(param.substring(0, 14).equals("oauth_verifier")){
							verifier = param.substring(15);
							Log.i("android2cloud", "verifier: "+verifier);
						}
						Log.i("android2cloud", param);
					}
					try {
						provider.retrieveAccessToken(consumer, verifier);
					} catch (OAuthMessageSignerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthNotAuthorizedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					intent.putExtra("token", consumer.getToken());
					intent.putExtra("secret", consumer.getTokenSecret());
					intent.putExtra("account", account);
					Log.i("android2cloud", "token: "+consumer.getToken());
					Log.i("android2cloud", "secret: "+consumer.getTokenSecret());
					setResult(1, intent);
					finish();
				}
			}
		});
		browser.loadUrl(oauth_request_url);
	}
	
	public static String getRequestURL(String host, Resources r){
        // create a new service provider object and configure it with
        // the URLs which provide request tokens, access tokens, and
        // the URL to which users are sent in order to grant permission
        // to your application to access protected resources
        consumer = new CommonsHttpOAuthConsumer(r.getString(R.string.consumer_key),
                r.getString(R.string.consumer_secret));
        provider = new CommonsHttpOAuthProvider(host+r.getString(R.string.request_url), host+r.getString(R.string.access_url), host+r.getString(R.string.authorize_url));

        // fetches a request token from the service provider and builds
        // a url based on AUTHORIZE_WEBSITE_URL and CALLBACK_URL to
        // which your app must now send the user
        String target = null;
        try {
			target = provider.retrieveRequestToken(consumer, host+r.getString(R.string.callback_url));
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			target = "OAuthMessageSignerException";
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			target = "OAuthNotAuthorizedException";
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			target = "OAuthExpectationFailedException";
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			target = "OAuthCommunicationException";
			e.printStackTrace();
		}
		return target;
	}
}