/**
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.android.redenvelope;

import android.app.Activity;
import android.content.Context;

//import com.aliyun.ams.ta.StatConfig;
//import com.aliyun.ams.ta.TA;
//import com.aliyun.ams.ta.Tracker;

import java.util.HashMap;
import java.util.Map;

public class AliUserTrackUtils { 

    private static final String TAG = "AliUserTrackUtils";
    private static final boolean DEBUG = true;
    
    // usertrack init
    public static void init(Context appContext) {
        if (DEBUG) {
            android.util.Log.d(TAG, "init");
        }
        /*StatConfig.getInstance().setContext(appContext);
        StatConfig.getInstance().turnOnDebug();
        Tracker lTracker = TA.getInstance().getTracker("21707682");
        TA.getInstance().setDefaultTracker(lTracker);

        if (DEBUG) {
            TA.getInstance().getDefaultTracker().updateUserAccount("yunos_settings", "yunos_settings");
        }*/
    }

    // usertrack release
    public static void uninit() {
        if (DEBUG) {
            android.util.Log.d(TAG, "uninit");
        }
        // TBS.uninit();
    }


    public static void resume(Object obj) {
        String pageName = getPageName(obj);
        if (DEBUG && pageName != null) {
            android.util.Log.d(TAG, "resume:" + pageName);
        }
        //TA.getInstance().getDefaultTracker().pageEnter(pageName);
    }
    public static void pause(Object obj) {
        String pageName = getPageName(obj);
        if (DEBUG && pageName != null) {
            android.util.Log.d(TAG, "pause:" + pageName);
        }
        //TA.getInstance().getDefaultTracker().pageLeave(pageName);
    }

    public static void pageResume(String pageName) {
        if (DEBUG && pageName != null) {
            android.util.Log.d(TAG, "page resume:" + pageName);
        }
        //TA.getInstance().getDefaultTracker().pageEnter(pageName);
    }

    public static void pagePause(String pageName) {
        if (DEBUG && pageName != null) {
            android.util.Log.d(TAG, "page pause:" + pageName);
        }
        //TA.getInstance().getDefaultTracker().pageLeave(pageName);
    }

    public static void goBack() {
        if (DEBUG) {
            android.util.Log.d(TAG, "go back");
        }
        // TBS.Page.goBack();
    }

    public static void click(Class<? extends Activity> pActivity, String ctrlName) {
        if (DEBUG && ctrlName != null) {
            android.util.Log.d(TAG, "activityName:" + pActivity.getSimpleName() + ", click:" + ctrlName);
        }
        //TA.getInstance().getDefaultTracker().ctrlClicked(pActivity, ctrlName);
    }

    public static void click(Class<? extends Activity> pActivity, String ctrlName, String key, String value) {
        if (DEBUG && ctrlName != null) {
            android.util.Log.d(TAG, "activityName:" + pActivity.getSimpleName() + ", click:" + ctrlName);
        }

        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put(key, value);
        //TA.getInstance().getDefaultTracker().ctrlClicked(pActivity, ctrlName, lMap);
    }

    public static void click(String pageName, String ctrlName) {
        if (DEBUG && ctrlName != null) {
            android.util.Log.d(TAG, "pageName:" + pageName + ", click:" + ctrlName);
        }
        //TA.getInstance().getDefaultTracker().commitEvent(pageName, 2101, ctrlName, null, null, null);
    }

    public static void click(String pageName, String ctrlName, String key, String value) {
        if (DEBUG && ctrlName != null) {
            android.util.Log.d(TAG, "pageName:" + pageName + ", click:" + ctrlName);
        }
        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put(key, value);
        //TA.getInstance().getDefaultTracker().commitEvent(pageName, 2101, ctrlName, null, null, lMap);
    }

    public static void click(String pageName, String ctrlName, String key1, String value1, String key2, String value2) {
        if (DEBUG && ctrlName != null) {
            android.util.Log.d(TAG, "pageName:" + pageName + ", click:" + ctrlName);
        }
        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put(key1, value1);
        lMap.put(key2, value2);
        //TA.getInstance().getDefaultTracker().commitEvent(pageName, 2101, ctrlName, null, null, lMap);
    }

    public static void click(String pageName, String ctrlName, boolean switchOn) {
        if (DEBUG && ctrlName != null) {
            android.util.Log.d(TAG, "pageName:" + pageName + ", click:" + ctrlName + ", switchOn:" + switchOn);
        }
        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put("switch", switchOn ? "true":"false");
        //TA.getInstance().getDefaultTracker().commitEvent(pageName, 2101, ctrlName, null, null, lMap);
    }

    public static void select(Class<? extends Activity> pActivity, String listName, int selectIndex) {
        if (DEBUG && listName != null) {
            android.util.Log.d(TAG, "activityName:" + pActivity.getSimpleName() + ", select:" + listName + ", selectIndex:" + selectIndex);
        }
        //TA.getInstance().getDefaultTracker().itemSelected(pActivity, listName, selectIndex);
    }

    public static void select(String pageName, String listName, int selectIndex) {
        if (DEBUG && listName != null) {
            android.util.Log.d(TAG, "pageName:" + pageName + ", select:" + listName + ", selectIndex:" + selectIndex);
        }
        //TA.getInstance().getDefaultTracker().commitEvent(pageName, 2101, listName, null, selectIndex, null);
    }

    public static void property(Object obj, String key, String value) {
        String pageName = getPageName(obj);
        if (DEBUG) {
            android.util.Log.d(TAG, "property:" + pageName + ", key:" + key + ", value:" + value);
        }
        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put(key, value);
        //TA.getInstance().getDefaultTracker().updatePageProperties(pageName, lMap);
    }

    public static void property(String customEvent, Map<String, String> map) {
        if (DEBUG) {
            android.util.Log.d(TAG, "customEvent:" + customEvent);
        }
        //TA.getInstance().getDefaultTracker().commitEvent(customEvent, map);
    }

    public static String getPageName(Object obj) {
        String pageName = obj.getClass().getSimpleName();
        return pageName;
    }

    /*public static String getIntentNameByHeaderId(int id) {
        String intentName = null;
        switch (id) {
        case R.id.wifi_settings: intentName = "wifi_settings"; break;
        case R.id.mobile_network_settings: intentName = "mobile_network_settings"; break;
        case R.id.bluetooth_settings: intentName = "bluetooth_settings"; break;
        case R.id.tether_settings: intentName = "tether_settings"; break;
        case R.id.wireless_settings: intentName = "wireless_settings"; break;

        case R.id.display_settings: intentName = "display_settings"; break;
        case R.id.audio_profile_settings: intentName = "audio_profile_settings"; break;
        case R.id.disturb_settings: intentName = "DoNotDisturb"; break;
        // case R.id.ali_lock_phone_setting: intentName = "ali_lock_phone_setting"; break;
        case R.id.face_settings: intentName = "face"; break;
        case R.id.notification_settings: intentName = "notification_settings"; break;
        //case R.id.lock_screen_weather_setting: intentName = "lock_screen_weather_setting"; break;

        case R.id.ali_account_setting: intentName = "ali_account_setting"; break;
        case R.id.ali_clound_sync_setting: intentName = "ali_clound_sync_setting"; break;
	//add by lishuisheng for gesture
	case R.id.ali_gesture_settings: intentName = "ali_gesture_settings"; break;

        case R.id.battery_settings: intentName = "battery_settings"; break;
        case R.id.storage_settings: intentName = "storage_settings"; break;
        case R.id.led_light_settings: intentName = "lamp_settings"; break;
        case R.id.multi_sim_settings: intentName = "multi_sim_settings"; break;
        //case R.id.phone_settings: intentName = "phone_settings"; break;

        case R.id.date_time_settings: intentName = "date_time_settings"; break;
        case R.id.language_settings: intentName = "language_settings"; break;
        case R.id.application_settings: intentName = "application_settings"; break;
        case R.id.about_settings: intentName = "about_settings"; break;
        case R.id.system_update_settings: intentName = "system_update_settings"; break;
        case R.id.security_settings: intentName = "security_settings"; break;
        case R.id.settings_more: intentName = "settings_more"; break;
        case R.id.device_section: intentName = "device_section"; break;
        *//*YUNOS BEGIN*//*
        //BugID:5609300
        //date:2014/12/01 ##author:yajun.xyj
        case R.id.search_settings: intentName = "search"; break;
        case R.id.ali_location_settings: intentName = "position"; break;
        case R.id.ali_aged_mode_setting: intentName = "seniormode"; break;
        *//*YUNOS END*//*
        default:
            intentName = "##";
        }
        return intentName;
    }*/
}
