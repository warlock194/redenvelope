package com.android.redenvelope;

import android.content.Context;
import android.content.SharedPreferences;
//import android.os.TopwiseConfig;
import android.provider.Settings;

public class Config {
	public static final String ACTION_RED_ENVELOPE_SERVICE_DISCONNECT = "com.android.redenvelope.ACCESSBILITY_DISCONNECT";
    public static final String ACTION_RED_ENVELOPE_SERVICE_CONNECT = "com.android.redenvelope.ACCESSBILITY_CONNECT";
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT = "com.android.redenvelope.NOTIFY_LISTENER_DISCONNECT";
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_CONNECT = "com.android.redenvelope.NOTIFY_LISTENER_CONNECT";
    public static final String ACTION_KEYGUARD_LOCK ="com.android.redenvelope.ACTION_KEYGUARD_LOCK";
    
    public static final String PREFERENCE_NAME = "config";
    public static final String KEY_ENABLE_RED_PACKET = "key_enable_red_packet";
    public static final String KEY_WECHAT_AFTER_OPEN_RED_ENVELOPE = "key_wechat_after_open_red_envelope";
    public static final String KEY_OPEN_DELAY_TIME = "key_open_delay_time";
    public static final String KEY_WECHAT_AFTER_GET_RED_ENVELOPE = "key_wechat_after_get_red_envelope";
    public static final String KEY_RED_PACKET_MODE = "key_red_packet_mode";
    public static final String KEY_AUTO_ANSWER = "key_auto_answer";
    public static final String KEY_RECEIVE_DELAY = "key_receive_delay";
    public static final String KEY_ENABLE_NOTIFY_AFTER_GET = "key_enable_notify_after_get";
    public static final String KEY_ALL_AUTO_RECORD = "key_auto_record_count";
    public static final String KEY_NOTIFICATION_SERVICE_ENABLE = "key_notification_service_enable";
    public static final String KEY_NOTIFY_SOUND = "key_notify_sound";
    public static final String KEY_NOTIFY_VIBRATE = "key_notify_vibrate";
    public static final String KEY_NOTIFY_NIGHT_ENABLE = "key_notify_night_enable";
    public static final String KEY_DEFINED_AUTO_REPLY = "key_defined_auto_reply";

    public static final int RED_PACKET_MODE_0 = 0;//自动抢
    public static final int RED_PACKET_MODE_1 = 1;//抢单聊红包,群聊红包只通知
    public static final int RED_PACKET_MODE_2 = 2;//抢群聊红包,单聊红包只通知
    public static final int RED_PACKET_MODE_3 = 3;//通知手动抢

    public static final long AUTO_ANSWSER_DELAY_TIME = 3000; //延迟自动回复时间（ms）
    public static final long LOCK_KEYGUARD_DELAY_TIME = 1000; //延迟锁屏时间（ms）
    public static final long SCREEN_OFF_DELAY_TIME = 1000; //延迟熄屏时间（ms）
    public static final long CHECK_IS_TOO_SLOW_DELAY_TIME = 1500;//去检测是否是手慢了的延时时间

    public static final int RANDOM_DELAY_TIME_START = 2;//随机延迟时间最小值
    public static final int RANDOM_DELAY_TIME_END = 8;//随机延迟时间最大值
    
    public static final boolean SETTING_WECHAT_MODE = false;
    public static final boolean SETTING_SHOW_NOTIFY = false;
    public static final boolean SUPPORT_UPDATE = true;
    public static final boolean SUPPORT_PLATFORM_CERTIFICATE = true;
    private static final boolean DEFAULT_ENABLE_RED_PACKET = false;
    private static final boolean DEFAULT_SOUND_NOTIFY = false;
    public static final boolean SUPPORT_DELAY_TIME_RANDOM = true;
    public static final boolean SHOW_UPDATE_PREFERENCE = true;
    private static Config current;
    
    //数据库中的字段名
    public static final String KEY_NAME = "name";
    public static final String KEY_TIME = "time";
    public static final String KEY_USED_TIME = "usedtime";
    public static final String KEY_MONEY = "money";
    
    public static synchronized Config getConfig(Context context) {
        if(current == null) {
            current = new Config(context.getApplicationContext());
        }
        return current;
    }

    private SharedPreferences preferences;
    private Context mContext;

    private Config(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /** 是否启动抢红包*/
    public boolean isEnableRedPacket() {
        return Settings.System.getInt(mContext.getContentResolver(), KEY_ENABLE_RED_PACKET,
                DEFAULT_ENABLE_RED_PACKET ? 1 : 0) == 1;
//        return preferences.getBoolean(KEY_ENABLE_RED_PACKET, DEFAULT_ENABLE_RED_PACKET) ;
    }

    public void setEnableRedPacket(boolean enable) {
        Settings.System.putInt(mContext.getContentResolver(), KEY_ENABLE_RED_PACKET, (enable ? 1:0));
//    	preferences.edit().putBoolean(KEY_ENABLE_RED_PACKET, enable).commit();
    }
    
    /** 打开红包后的事件*/
    public int getWechatAfterOpenRedEnvelopeEvent() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_AFTER_OPEN_RED_ENVELOPE, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 微信抢到红包后的事件*/
    public int getWechatAfterGetRedEnvelopeEvent() {
        int defaultValue = 1;
        String result =  preferences.getString(KEY_WECHAT_AFTER_GET_RED_ENVELOPE, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /**延时打开红包时间*/
    public int getOpenDelayTime() {
        int defaultValue = 0;
        defaultValue = preferences.getInt(KEY_OPEN_DELAY_TIME, defaultValue);
        return defaultValue;
    }
    
    
    public void setOpenDelayTime(int delayTime) {
        preferences.edit().putInt(KEY_OPEN_DELAY_TIME, delayTime).commit();
    }
    
    /** 延时打开红包时间选项*/
    public int getReceiveDelayValue() {
        int defaultValue = 0;
        if (SUPPORT_DELAY_TIME_RANDOM) {
            defaultValue = 3;//2-8s
        }
        defaultValue = preferences.getInt(KEY_RECEIVE_DELAY, defaultValue);
        return defaultValue;
    }
    
    
    public void setReceiveDelayValue(int value) {
        preferences.edit().putInt(KEY_RECEIVE_DELAY, value).commit();
    }

    /** 获取抢红包的模式*/
    public int getRedPacketMode() {
        int defaultValue = RED_PACKET_MODE_2;
        defaultValue = preferences.getInt(KEY_RED_PACKET_MODE, defaultValue);
        return defaultValue;
    }
    
    public void setRedPacketMode(int value) {
    	preferences.edit().putInt(KEY_RED_PACKET_MODE, value).commit();
    }
    
    /** 是否启动通知栏模式*/
    public boolean isEnableNotificationService() {
        return preferences.getBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, false);
    }

    public void setNotificationServiceEnable(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, enable).apply();
    }

    /** 是否开启声音*/
    public boolean isNotifySound() {
        return preferences.getBoolean(KEY_NOTIFY_SOUND, false);
    }

    public void setNotifySound(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFY_SOUND, enable).commit();
    }

    /** 是否开启震动*/
    public boolean isNotifyVibrate() {
        return preferences.getBoolean(KEY_NOTIFY_VIBRATE, false);
    }

    public void setNotifyVibrate(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFY_VIBRATE, enable).commit();
    }

    /** 是否开启夜间免打扰模式*/
    public boolean isNotifyNight() {
        return preferences.getBoolean(KEY_NOTIFY_NIGHT_ENABLE, false);
    }
    
    public void setNotifyNight(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFY_NIGHT_ENABLE, enable).commit();
    }
    
    /**设置抢到红包时是否自动回复内容**/
    public void setAutoAnswer(int value) {
    	preferences.edit().putInt(KEY_AUTO_ANSWER, value).commit();
    }
    
    public int getAutoAnswer() {
    	int defaultValue = 0;
        defaultValue = preferences.getInt(KEY_AUTO_ANSWER, defaultValue);
    	return defaultValue;
    }
    
    /**设置抢到红包时是否有声音提醒**/
    public void setNotifyAfterGet(boolean enalbe) {
    	preferences.edit().putBoolean(KEY_ENABLE_NOTIFY_AFTER_GET, enalbe).commit();
    }
    
    public boolean isNotifyAfterGet() {
    	boolean defaultEnable = DEFAULT_SOUND_NOTIFY;
    	defaultEnable = preferences.getBoolean(KEY_ENABLE_NOTIFY_AFTER_GET, defaultEnable);
    	return defaultEnable;
    }
    
    public void setAutoRobCount() {
    	int value = preferences.getInt(KEY_ALL_AUTO_RECORD, 0);
    	preferences.edit().putInt(KEY_ALL_AUTO_RECORD, value+1).commit();
    }
    
    public int getAutoRobCount() {
    	int value = preferences.getInt(KEY_ALL_AUTO_RECORD, 0);
    	return value;
    }

    public void clearAutoRobcount() {
    	preferences.edit().putInt(KEY_ALL_AUTO_RECORD, 0).commit();
    }

    public void setDefinedAutoReply(String replyStr) {
        preferences.edit().putString(KEY_DEFINED_AUTO_REPLY, replyStr).commit();
    }

    public String getDefinedAutoReply() {
        return preferences.getString(KEY_DEFINED_AUTO_REPLY, null);
    }
}
