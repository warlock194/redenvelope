package com.android.redenvelope;


import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class MMShortcutService extends AccessibilityService {
    public static final String CMD_FLOATSHORTCUT_OPEN = "com.topwise.mmshort.open";
    public static final String CMD_FLOATSHORTCUT_CLOSE = "com.topwise.mmsort.close";
    private MMShortcutBar mFloatShortcutBar;
    private MMShortcutPlay mmShortcutPlay;

    public static final String TAG = "TrWechatAccessiblity";

    //start by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
    private String MM_ACTION = "com.android.action.MMVIDEO";
    private Intent intent;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        IntentFilter filter = new IntentFilter();
        filter.addAction(MM_ACTION);
        registerReceiver(mVideoReceiver,filter);
        mFloatShortcutBar = new MMShortcutBar(this,getApplicationContext());
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    //end by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        String cmd = intent.getAction();

        if (cmd == null) {
            return START_REDELIVER_INTENT;
        }

        if (cmd.equals(CMD_FLOATSHORTCUT_OPEN)) {

            int x = intent.getIntExtra("shortcut_x", -1);
            int y = intent.getIntExtra("shortcut_y", -1);
            int clickTime = intent.getIntExtra("clicktime", 1);

            if (intent.getBooleanExtra("shortcut_play", false)) {

                onOpenPlay();

            } else {

                onOpen(x, y, clickTime);
            }


        } else if (cmd.equals(CMD_FLOATSHORTCUT_CLOSE)) {

            if (intent.getBooleanExtra("shortcut_play", false)) {

                onClosePlay();

            } else {

                onClose();
            }


        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (mFloatShortcutBar != null) {
            mFloatShortcutBar.onClose();
        }

        if(mmShortcutPlay != null) {
            mmShortcutPlay.onClose();
        }
        //start by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
        unregisterReceiver(mVideoReceiver);
        //end by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
    }


    private void onOpenPlay() {
        if (mmShortcutPlay != null) {
            mmShortcutPlay.onOpen();
            Log.v(TAG, "onOpen(),mMultiTasks != null ");
        } else {
            mmShortcutPlay = new MMShortcutPlay(getApplicationContext());
            mmShortcutPlay.onOpen();
            Log.v(TAG, "onOpen(),mMultiTasks == null ");
        }
        //start by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
        mmShortcutPlay.setPath(mmVideoPath);
        mmShortcutPlay.setUserId(UserId);
        UserId = 0;
        //end by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
    }

    private void onClosePlay() {
        if (mmShortcutPlay != null) {
            mmShortcutPlay.onClose();
            Log.v(TAG, "onClose()");
        }
    }


    private void onOpen(int x, int y, int clickTime) {
        if (mFloatShortcutBar != null) {
            mFloatShortcutBar.onOpen(x, y, clickTime);
            Log.v(TAG, "onOpen(),mMultiTasks != null ");
        } else {
//            mFloatShortcutBar = new MMShortcutBar(this,getApplicationContext());
            mFloatShortcutBar.onOpen(x, y, clickTime);
            Log.v(TAG, "onOpen(),mMultiTasks == null ");
        }
    }

    private void onClose() {
        if (mFloatShortcutBar != null) {
            mFloatShortcutBar.onClose();
            Log.v(TAG, "onClose()");
        }
    }

    //start by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
    private String mmVideoPath;
    private int UserId = 0 ;
    private BroadcastReceiver mVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mmVideoPath = intent.getStringExtra("path");
            UserId = intent.getIntExtra("userid",0);
            Log.d(TAG,"  userId = " + UserId + "  mmVideoPath = " + mmVideoPath );
        }
    };
    //end by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
}
