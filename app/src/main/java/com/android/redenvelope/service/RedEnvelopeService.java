package com.android.redenvelope.service;


import java.util.Iterator;
import java.util.List;

import com.android.redenvelope.AccessibilityHelper;
import com.android.redenvelope.Config;
import com.android.redenvelope.NotifyHelper;
import com.android.redenvelope.QQAccessibility;
import com.android.redenvelope.QQConfig;
import com.android.redenvelope.WechatAccessiblity;
import com.android.redenvelope.WechatConfig;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class RedEnvelopeService extends AccessibilityService{

	private static final String TAG = "RedEnvelopeService";
	private static RedEnvelopeService mService;
	private WechatAccessiblity mWechatAccessiblity;
	private QQAccessibility mQQAccessiblity;
	
    @Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	super.onCreate();
    	mWechatAccessiblity = new WechatAccessiblity(this, getApplicationContext());
    	mQQAccessiblity = new QQAccessibility(this, getApplicationContext());
    }
    
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
        String packageName = String.valueOf(event.getPackageName());
        boolean isEnable = Config.getConfig(getApplicationContext()).isEnableRedPacket();
		Log.d(TAG, "isEnable : " + isEnable + "  packageName : " + packageName + " mWechatAccessiblity : " + mWechatAccessiblity);
        if(WechatConfig.WECHAT_PACKAGENAME.equals(packageName)
        		&& null != mWechatAccessiblity) {
            mWechatAccessiblity.onReceiveAccessibilityEvent(event, isEnable);
        } else if (isEnable && QQConfig.QQ_PACKAGENAME.equals(packageName)
        		&& null != mQQAccessiblity){
        	mQQAccessiblity.onReceiveAccessibilityEvent(event);
        }
        
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
//		Toast.makeText(this, "AccessibilityService is disconnected", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "RedEnvelopeService is interrupt");
	}

	public static boolean isRunning() {
		if(mService == null) {
			Log.d(TAG, "isRunning mService : " + mService);
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) mService.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = mService.getServiceInfo();
        if(info == null) {
			Log.d(TAG, "isRunning info : " + info);
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if(i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
		Log.d(TAG, "isRunning isConnect : " + isConnect);
        if(!isConnect) {
            return false;
        }
        return true;
		
	}

	public static boolean isNotificationServiceRunning() {
		// TODO Auto-generated method stub
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
			return false;
		return true;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//accessibility service is disconnect
		Intent mIntent = new Intent(Config.ACTION_RED_ENVELOPE_SERVICE_DISCONNECT);
		sendBroadcast(mIntent);
		mService = null;
	}
	
	@Override
	protected void onServiceConnected() {
		// TODO Auto-generated method stub
		super.onServiceConnected();
		mService = this;
		//accessibility service is connect
		Intent mIntent = new Intent(Config.ACTION_RED_ENVELOPE_SERVICE_CONNECT);
		sendBroadcast(mIntent);
//		Toast.makeText(this, "AccessibilityService is connected", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "RedEnvelopeService is connected");
	}
	
}
