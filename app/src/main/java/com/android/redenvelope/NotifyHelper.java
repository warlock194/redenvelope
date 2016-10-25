package com.android.redenvelope;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import java.util.Calendar;


public class NotifyHelper {
	private static final String TAG = "NotifyHelper_Log";
    private static Vibrator mVibrator;
    private static KeyguardManager mKeyguardManager;
    private static PowerManager mPowerManager;
    private static NotificationManager mNotificationManager;

    /** 播放声音*/
    public static void sound(Context context) {
        try {
            //start, add by zl topwise for bug[4946]
            /*MediaPlayer player = MediaPlayer.create(context,
                    Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));*/
            MediaPlayer player = MediaPlayer.create(context, R.raw.redpacket);
            //end, add by zl topwise for bug[4946]
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 振动*/
    public static void vibrator(Context context) {
        if(mVibrator == null) {
        	mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(new long[]{100, 10, 100, 1000}, -1);
    }

    /** 是否为夜间*/
    public static  boolean isNightTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour >= 23 || hour < 7) {
            return true;
        }
        return false;
    }

    public static KeyguardManager getKeyguardManager(Context context) {
        if(mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
        
        return mKeyguardManager;
    }

    public static PowerManager getPowerManager(Context context) {
        if(mPowerManager == null) {
            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        return mPowerManager;
    }

    /** 是否为锁屏状态*/
    public static boolean isLockScreen(Context context) {
        KeyguardManager km = getKeyguardManager(context);
        
        return km.isKeyguardLocked();
    }

    @SuppressLint("NewApi")
	public static boolean isScreenOn(Context context) {
        PowerManager pm = getPowerManager(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            return pm.isScreenOn();
        }
    }

    /** 播放效果、声音与震动*/
    public static void playEffect(Context context, Config config) {
        //夜间模式，不处理
        if(NotifyHelper.isNightTime() && config.isNotifyNight()) {
            return;
        }

        if(config.isNotifySound()) {
            sound(context);
        }
        if(config.isNotifyVibrate()) {
            vibrator(context);
        }
    }

    /** 显示通知*/
    public static void showNotify(Context context, String text, PendingIntent pendingIntent) {
    	Resources r = context.getResources();
    	Notification.Builder notifyBuilder = new Notification.Builder(context)
    		.setSmallIcon(R.drawable.ic_launcher)
    		.setContentTitle(r.getString(R.string.app_name))
    		.setContentText(text)
    		.setContentIntent(pendingIntent);
    	
    	Notification notification = notifyBuilder.build();
    	getNotificationManager(context).notify(1, notification);
    }
    
    public static void showNotify(Context context, String text, 
    		long when, PendingIntent pendingIntent) {
    	Resources r = context.getResources();
    	Notification.Builder notifyBuilder = new Notification.Builder(context)
    		.setSmallIcon(R.drawable.red_packet_notification)
    		.setContentTitle(r.getString(R.string.app_name))
    		.setContentText(text)
    		.setWhen(when)
    		.setContentIntent(pendingIntent);
    	
    	Notification notification = notifyBuilder.build();
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	getNotificationManager(context).notify(1, notification);
    }

    /** 执行PendingIntent事件*/
    public static void send(PendingIntent pendingIntent) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
    
    public static NotificationManager getNotificationManager(Context context) {
        if(mNotificationManager == null) {
        	mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    
    public static boolean handleKeyguareDone(Context context, PendingIntent mPendingIntent) {
    	try {
			Intent intent = new Intent(context, DismissKeyguardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("pendingIntent", mPendingIntent);
			context.startActivity(intent);
			Log.i(TAG, "handleKeyguareDone startActivity");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "handleKeyguareDone exception : " + e.toString());
			return false;
		}
    	return true;
    }
    
    public static void handleScreenOn(Context context) {
        Log.i(TAG, "handleScreenOn " );
        PowerManager pm = getPowerManager(context);
        //mPM.wakeUp(SystemClock.uptimeMillis());
    	WakeLock mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
        		|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE, "reevnelope wakeup");
        mWakeLock.acquire();
        mWakeLock.release();
    }
    
    public static void handleScreenOff(Context context) {
    	PowerManager pm = getPowerManager(context);
    	pm.goToSleep(SystemClock.uptimeMillis());
    }
    
    public static void handleKeyguardLock(final Context context) {
		Intent intent = new Intent();
		intent.setAction(Config.ACTION_KEYGUARD_LOCK);
		context.sendBroadcast(intent);
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
            	handleScreenOff(context);
            }
        }, Config.SCREEN_OFF_DELAY_TIME);
		
	}
    
    public static void playSound(Context context) {
		// TODO Auto-generated method stub
		boolean isPlaySound = Config.getConfig(context).isNotifyAfterGet();
		boolean isNightEnable = Config.getConfig(context).isNotifyNight() && NotifyHelper.isNightTime();
		if (isPlaySound && !isNightEnable) {
			sound(context);
		}
	}
}
