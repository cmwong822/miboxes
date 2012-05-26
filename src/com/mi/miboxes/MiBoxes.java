package com.mi.miboxes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class MiBoxes extends Activity {
	private static final String TAG = "MiBoxes";

	final static private String APP_KEY = "78b0t715n35pxjt";
	final static private String APP_SECRET = "dhjfnf1ijo21n4y";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	DropboxAPI<AndroidAuthSession> mApi;

	private boolean mLoggedIn;

	private Button mAddAccount;
	private ImageButton mAccount1;
	private TextView mAccName1;
	private TextView mAccInfo1;
	private Account mAcc;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// AuthSession for using DropBox API.
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		setContentView(R.layout.main);

		mAddAccount = (Button) findViewById(R.id.btnAddAccount);
		mAddAccount.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mApi.getSession().startAuthentication(MiBoxes.this);
			}
		});

		mAccName1 = (TextView) findViewById(R.id.txtAccName1);
		mAccInfo1 = (TextView) findViewById(R.id.txtAccInfo1);

		setLoggedIn(mApi.getSession().isLinked());

	}

	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;

		if (loggedIn) {
			mAccName1.setText("Logined!");
				new DisplayAccountInfoTask().execute();
			
		} else {
			mAccName1.setText("Please Login!");
			mAccInfo1.setText("");

		}
	}

	class DisplayAccountInfoTask extends AsyncTask<String, Integer, String> {

		String accInfo;
		Long quota;

		@Override
		protected String doInBackground(String... params) {
			try {
				Log.d("AccInfo", "Before");
				accInfo = mApi.accountInfo().displayName;
				// mAccName1.setText(accInfo);
				Log.d("AccInfo", "After");
				return accInfo;

			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("AccInfo Error", e.getLocalizedMessage());
				return null;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				mAccName1.setText(accInfo);
			} else {
				mAccName1.setText("Shit! Error in doInBackground");
			}

		}

	}

	private void logOut() {
		mApi.getSession().unlink();

		clearKeys();

		setLoggedIn(false);

	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = mApi.getSession();

		if (session.authenticationSuccessful()) {
			try {
				session.finishAuthentication();
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				setLoggedIn(true);

			} catch (IllegalStateException e) {
				showToast("Couldn't authenticate with Dropbox:"
						+ e.getLocalizedMessage());
				Log.i(TAG, "Error authenticating", e);
			}
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0],
					stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,
					accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}

	private String[] getKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	private void storeKeys(String key, String secret) {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
}