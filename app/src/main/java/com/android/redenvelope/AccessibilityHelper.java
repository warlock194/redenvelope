package com.android.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.reflect.Field;
import java.util.List;

//import android.app.IActivityManager;
//import android.view.IWindowManager;


@SuppressLint("NewApi")
public final class AccessibilityHelper {

    private AccessibilityHelper() {}

    /** 通过id查找*/
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if(list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    /** 通过文本查找*/
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /** 通过关键字查找*/
    public static AccessibilityNodeInfo findNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String... texts) {
        for(String key : texts) {
            AccessibilityNodeInfo info = findNodeInfosByText(nodeInfo, key);
            if(info != null) {
                return info;
            }
        }
        return null;
    }

    /** 通过组件名字查找*/
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if(TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if(className.equals(node.getClassName())) {
                return node;
            }
        }
        return null;
    }

    /** 找父组件*/
    public static AccessibilityNodeInfo findParentNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if(nodeInfo == null) {
            return null;
        }
        if(TextUtils.isEmpty(className)) {
            return null;
        }
        if(className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeInfosByClassName(nodeInfo.getParent(), className);
    }

    private static final Field sSourceNodeField;

    static {
        Field field = null;
        try {
            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sSourceNodeField = field;
    }

    public static long getSourceNodeId (AccessibilityNodeInfo nodeInfo) {
        if(sSourceNodeField == null) {
            return -1;
        }
        try {
            return sSourceNodeField.getLong(nodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    /** 返回主界面事件*/
    public static boolean performHome(AccessibilityService service) {
        if(service == null) {
            return false;
        }
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /** 返回事件*/
    public static boolean performBack(AccessibilityService service) {
        if(service == null) {
            return false;
        }
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /** 点击事件*/
    public static boolean performClick(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return false;
        }
        boolean isSuc;
        if(nodeInfo.isClickable()) {
        	isSuc = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
        	isSuc = performClick(nodeInfo.getParent());
        }
        return isSuc;
    }
    
    public static boolean nodeInfoIsClickAble(AccessibilityNodeInfo nodeInfo) {
    	if(nodeInfo == null) {
            return false;
        }
        if(nodeInfo.isClickable()) {
        	return true;
        } else {
        	nodeInfoIsClickAble(nodeInfo.getParent());
        }
        return false;
    }
    
    public static void performAction(AccessibilityNodeInfo nodeInfo, Bundle arguments) {
        if(nodeInfo == null) {
            return;
        }
//        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

 /*   public static void perfermTouch(Rect postion, IWindowManager mWm, IActivityManager mAm) {

        com.android.redenvelope.MonkeyMotionEvent downEvent = new com.android.redenvelope.MonkeyTouchEvent(MotionEvent.ACTION_DOWN)
                .addPointer(0, postion.centerX(), postion.centerY());
        com.android.redenvelope.MonkeyMotionEvent upEvent = new com.android.redenvelope.MonkeyTouchEvent(MotionEvent.ACTION_UP)
                .addPointer(0, postion.centerX(), postion.centerY());

        if (mAm != null && mWm != null) {
            downEvent.injectEvent(mWm , mAm, 1);
            upEvent.injectEvent(mWm, mAm , 1);
        }

    }*/

}
