package com.suchagit.android2cloud;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.suchagit.android2cloud.R;

public class AccountAdd extends Activity {
	protected static final int OAUTH_REQ_CODE = 0x1122;
	protected String ACCOUNTS_PREFERENCES = "android2cloud-accounts";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_account);
		final EditText account_input = (EditText) findViewById(R.id.account_entry);
		final EditText host_input = (EditText) findViewById(R.id.host_entry);
		final Intent intent = this.getIntent();
		SharedPreferences accounts = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
		if(intent.getExtras() != null && intent.getExtras().getString("account") != null){
			account_input.setText((CharSequence) intent.getExtras().get("account"));
			host_input.setText((CharSequence) accounts.getString(intent.getExtras().getString("account"), "||error").split("\\|")[2]);
		}else{
			account_input.setText("Default");
			host_input.setText("http://android2cloud.appspot.com");
		}
		final Button submit_button = (Button) findViewById(R.id.go);
		final Button cancel_button = (Button) findViewById(R.id.cancel);
		submit_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String account_text = account_input.getText().toString();
				String host_text = host_input.getText().toString();
    			Intent i = new Intent(AccountAdd.this, OAuth.class);
    			i.putExtra("account", account_text);
    			i.putExtra("host", host_text);
    			startActivityForResult(i, OAUTH_REQ_CODE);
			}
		});
		cancel_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(0);
				finish();
			}
		});
	}
	
    @Override
	protected void onActivityResult(int req_code, int res_code, Intent intent) {
    	super.onActivityResult(req_code, res_code, intent);
    	if(res_code == 1){
			SharedPreferences accounts = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
			SharedPreferences.Editor editor = accounts.edit();
			if(intent.getExtras() != null && intent.getExtras().getString("account") != null){
				editor.remove(intent.getExtras().getString("account"));
			}
			editor.putString(intent.getExtras().getString("account"), intent.getExtras().getString("token")+"|"+intent.getExtras().getString("secret")+"|"+intent.getExtras().getString("host"));
			editor.commit();
			//Toast.makeText(AccountAdd.this, "Adding "+account_text+" on "+host_text+" to accounts.", Toast.LENGTH_LONG).show();
			setResult(1);
			finish();
    	}
    }
}