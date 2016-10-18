package com.android.redenvelope;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class DismissKeyguardActivity extends Activity{
	private static final String TAG = "RedEnvelopeDismissKeyguard";
	private KeyguardManager mKeyguardManager;
	private KeyguardLock keyguardLock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dismiss_keyguard);
//		Log.i(TAG, "DismissKeyguardActivity onCreate");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mKeyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
		keyguardLock = mKeyguardManager.newKeyguardLock("");
    	keyguardLock.disableKeyguard(); //If the keyguard is currently showing, hide it
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(Config.ACTION_KEYGUARD_LOCK);
    	filter.addAction(Intent.ACTION_SCREEN_OFF);
    	filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
    	registerReceiver(mKeyguardLockRecevier, filter);
    	
    	PendingIntent mPendingIntent = (PendingIntent) getIntent().getExtras().get("pendingIntent");
    	Log.i(TAG, "DismissKeyguardActivity onCreate mPendingIntent : " + mPendingIntent);
    	if (null != mPendingIntent) {
    		NotifyHelper.send(mPendingIntent);
    	}
	}
	
	private BroadcastReceiver mKeyguardLockRecevier = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
//			Log.i(TAG, "DismissKeyguardActivity action : " + action);
			if (Config.ACTION_KEYGUARD_LOCK.equals(action)
					|| Intent.ACTION_SCREEN_OFF.equals(action)) {
				finish();
			}
			
		}
		
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				finish();
			}
		}, 8000);*/
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		Log.i(TAG, "DismissKeyguardActivity onDestroy");
		if (keyguardLock != null ) {
			keyguardLock.reenableKeyguard(); //show the hide keyguard
		}
		if (null != mKeyguardLockRecevier) {
			unregisterReceiver(mKeyguardLockRecevier);
		}
		mKeyguardLockRecevier = null;
		mKeyguardManager = null;
	}
}
