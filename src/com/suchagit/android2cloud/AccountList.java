package com.suchagit.android2cloud;

import java.util.Map;

import com.suchagit.android2cloud.R;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountList extends ListActivity {

	protected Intent intent;
	
	protected String ACCOUNTS_PREFERENCES = "android2cloud-accounts";
	protected String SETTINGS_PREFERENCES = "android2cloud-settings";
	
	private static final int ACCOUNT_ADD_REQ_CODE = 0x1234;
	private static final int EDIT_ID = 0x2345;
	private static final int DELETE_ID = 0x3456;
	
	protected String[] accounts_array;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(SETTINGS_PREFERENCES, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("account");
		editor.remove("host");
		editor.remove("token");
		editor.remove("secret");
		editor.commit();
        SharedPreferences accounts_prefs = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
        Map<String, ?> accounts = accounts_prefs.getAll();
        int size = accounts.size();
        Object[] account_objects_array = accounts.keySet().toArray();
        if(size == 1){
        	Log.i("android2cloud", account_objects_array[0].toString());
			SharedPreferences.Editor settings_editor = settings.edit();
			String[] tmpToken = accounts.get(account_objects_array[0].toString()).toString().split("\\|");
			settings_editor.putString("account", account_objects_array[0].toString());
			settings_editor.putString("host", tmpToken[2]);
			settings_editor.putString("token", tmpToken[0]);
			settings_editor.putString("secret", tmpToken[1]);
			settings_editor.commit();
			Log.i("android2cloud", "account: "+account_objects_array[0].toString());
			Log.i("android2cloud", "host: "+tmpToken[2]);
			Log.i("android2cloud", "token: "+tmpToken[0]);
			Log.i("android2cloud", "secret: "+tmpToken[1]);
        }
        Log.i("android2cloud", "size: "+size);
        size++;
        Log.i("android2cloud", "size++: "+size);
        accounts_array = new String[size];
        Log.i("android2cloud", "account_objects_array: "+account_objects_array.length);
        accounts_array[0] = "Add New Account";
       	for(int x=0; x<account_objects_array.length; x++){
        	Log.i("android2cloud", "index: "+x);
        	accounts_array[x+1] = (String) account_objects_array[x];
        }
        Log.i("android2cloud", "setting list adapter");
        this.setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, accounts_array));
        registerForContextMenu(getListView());
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, EDIT_ID, 0, "Edit");
    	menu.add(0, DELETE_ID, 0,  "Delete");
    }

    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		SharedPreferences accounts = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
		SharedPreferences.Editor editor = accounts.edit();
    	switch (item.getItemId()) {
    		case EDIT_ID:
    			//Toast.makeText(this, "Edit item "+accounts_array[(int) info.id], Toast.LENGTH_LONG).show();
    			Intent i = new Intent(AccountList.this, AccountAdd.class);
    			String account_info = accounts_array[(int) info.id];
    			i.putExtra("account", account_info);
    			startActivityForResult(i, ACCOUNT_ADD_REQ_CODE);     
    			return true;
    		case DELETE_ID:
    			editor.remove(accounts_array[(int) info.id]);
    			editor.commit();
    			//Toast.makeText(this, "Deleted item "+accounts_array[(int) info.id], Toast.LENGTH_LONG).show();
    			onCreate(null);
    			return true;
    		default:
    			return super.onContextItemSelected(item);
    	}
    }

    @Override
	protected void onActivityResult(int req_code, int res_code, Intent intent) {
        super.onActivityResult(req_code, res_code, intent);
        if(res_code == 1){
	        SharedPreferences accounts_prefs = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
	        Map<String, ?> accounts = accounts_prefs.getAll();
	        int size = accounts.size();
	        Object[] account_objects_array = accounts.keySet().toArray();
	        Log.i("android2cloud", "size: "+size);
	        size++;
	        Log.i("android2cloud", "size++: "+size);
	        accounts_array = new String[size];
	        Log.i("android2cloud", "account_objects_array: "+account_objects_array.length);
	        accounts_array[0] = "Add New Account";
	       	for(int x=0; x<account_objects_array.length; x++){
	        	Log.i("android2cloud", "index: "+x);
	        	accounts_array[x+1] = (String) account_objects_array[x];
	        }
	        Log.i("android2cloud", "setting list adapter");
	        this.setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, accounts_array));        
        }
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position != 0){
			String account = (String)getListView().getItemAtPosition(position);
			Intent intent = new Intent(this, PostLink.class);
			intent.putExtra("account", account);
	        SharedPreferences accounts_prefs = getSharedPreferences(ACCOUNTS_PREFERENCES, 0);
			intent.putExtra("token", accounts_prefs.getString(account, "error"));
			//Toast.makeText(AccountList.this, accounts_prefs.getString(account, "error"), Toast.LENGTH_LONG).show();
			setResult(1, intent);
			finish();
		}else{
			Intent intent = new Intent(this, AccountAdd.class);
			startActivityForResult(intent, ACCOUNT_ADD_REQ_CODE);
		}
	}
}