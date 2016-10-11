package com.android.redenvelope;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.android.redenvelope.database.RedEnvelopeDBHelper;
import com.android.redenvelope.service.RedEnvelopeService;

public class QQAccessibility {
	private static final String TAG = "QQAccessibility";
	private RedEnvelopeService mService;
	private Context mContext;
	private boolean isNeedReturnHome = false;
	private long openredEnvelopetime = 0;   //拆開红包的时间
    private long onClickRedEnveloptime = 0; //在信息列表点击红包的时间
    private boolean isRobRedEnvelopeSuc = false;
    private boolean isNewRedEnvelope = false;
    private boolean isFinishRobEnvelope = true; //判断抢红包是否已经完成
	private boolean isAutoReturn = false;  //自动打开的红包是否自动返回
	private boolean isDetailUi = false;
	private boolean isChatListReturn = false;
	 private boolean isLoadSuc = false; //判断拆红包是否成功
	
	//待抢节点列表
    private ArrayList<AccessibilityNodeInfo> mNewNodeInfoList = new ArrayList<AccessibilityNodeInfo>();
	
	public QQAccessibility(RedEnvelopeService service, Context context) {
		mService = service;
		mContext = context;
	}
	
	public void onReceiveAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();
		
		Log.i(TAG, "eventTypeStr = " + AccessibilityEvent.eventTypeToString(eventType));
		switch (eventType) {
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: //64
				notificationEvent(event);
				break;
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //32
				openRedEnvelope(event);
				break;
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  //2048
				handleChatListRedEnvelope(event);
				break;
			default:
				break;
		}
	}

	private void notificationEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		Parcelable data =  event.getParcelableData();
		if (null == data || !(data instanceof Notification)) {
			Log.w(TAG, "notifcationEvent return");
			return;
		}
		
		List<CharSequence> mText = event.getText();
		if (!mText.isEmpty()) {
			for (CharSequence c : mText) {
                String str = String.valueOf(c);
                Log.i(TAG, "notificationEvent str = " + str);
                if (str.contains(QQConfig.QQ_NOTIFY_TEXT)) {
                    openNotification((Notification)data);
                    break;
                }
            }
		}
	}

	private void openNotification(Notification notification) {
		// TODO Auto-generated method stub
		PendingIntent mPendingIntent = notification.contentIntent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (!QQConfig.QQ_PACKAGENAME.equals(mPendingIntent.getCreatorPackage())) {
                Log.w(TAG, "openNotification return");
                return;
            }
		}

		boolean isLock = NotifyHelper.isLockScreen(mContext);
		boolean isScreenOn = NotifyHelper.isScreenOn(mContext);
		int mode = Config.getConfig(mContext).getRedPacketMode();
		boolean justNotify = mode == Config.RED_PACKET_MODE_3;
		Log.i(TAG, "isLock : " + isLock + "   isScreenOn : " + isScreenOn + "  justNotify : " + justNotify);
		if (!isLock && isScreenOn && !justNotify) {  //keyguard is unlock and screen is on
			NotifyHelper.send(mPendingIntent);
		} else if (isLock && !isScreenOn && !justNotify){ //keyguard is lock and screen is off
			NotifyHelper.handleScreenOn(mContext);
			NotifyHelper.handleKeyguareDone(mContext, mPendingIntent);
			isNeedReturnHome = true;
		}else if (!isLock && !isScreenOn && !justNotify) { //keyguard is unlock and screen is off
			NotifyHelper.handleScreenOn(mContext);
			NotifyHelper.send(mPendingIntent);
			isNeedReturnHome = true;
		} else if (isLock && isScreenOn && !justNotify) {  //keyguard is lock and screen is on
			NotifyHelper.handleKeyguareDone(mContext, mPendingIntent);
			isNeedReturnHome = true;
		} else {
			NotifyHelper.showNotify(mContext, String.valueOf(notification.tickerText), mPendingIntent);
		}
		
		if(mode != Config.RED_PACKET_MODE_0) {
            NotifyHelper.playEffect(mContext, Config.getConfig(mContext));
        }
		
	}
	
	private void openRedEnvelope(AccessibilityEvent event) {
		String classNmae = String.valueOf(event.getClassName());
		Log.d(TAG, "openRedEnvelope class name : " + classNmae);
		if (QQConfig.QQ_LAUNCH_UI.equals(classNmae)) {
			handleLauncheUI(event);
		} else if (QQConfig.QQ_WALLETPLUGIN_UI.equals(classNmae)) {
			handleDetailUI();
		}
	}
	
	private void handleLauncheUI(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		isAutoReturn = false;
		handleChatListRedEnvelope(event);
		//for auto answer
		int autoAnswerType = Config.getConfig(mContext).getAutoAnswer();
		if (0 != autoAnswerType && isRobRedEnvelopeSuc) {
			handleAutoAnswer(getAutoAnswerStr());
		}
		
		if (isFinishRobEnvelope) {
			int newNodeListSize = mNewNodeInfoList.size();
			if (newNodeListSize > 0) {
				AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
		        if(nodeInfo == null) {
		            Log.w(TAG, "handleLauncheUI nodeInfo is null");
		            return;
		        }
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					nodeInfo.refresh();
				}

				clickNewNodeInfo(mNewNodeInfoList.get(newNodeListSize-1));
				mNewNodeInfoList.remove(newNodeListSize-1);
			} 
		}
		
		//for determine whether to return to the home and lock screen
		Log.i(TAG, "isDetailUi : " + isDetailUi + "  isNeedReturnHome : " + isNeedReturnHome );
		Log.i(TAG, "isChatListReturn : " + isChatListReturn + "  isFinishRobEnvelope : " + isFinishRobEnvelope );
		if ((isDetailUi && isNeedReturnHome && isFinishRobEnvelope)
				|| (isNeedReturnHome && isChatListReturn && isFinishRobEnvelope)) {
			AccessibilityHelper.performBack(mService);
			AccessibilityHelper.performHome(mService);
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                	NotifyHelper.handleKeyguardLock(mContext);
                }
            }, Config.LOCK_KEYGUARD_DELAY_TIME);
			isNeedReturnHome = false;
		}
		isChatListReturn = false;
		isDetailUi = false;
		isRobRedEnvelopeSuc = false;
	}

	private void handleDetailUI() {
		openredEnvelopetime = System.currentTimeMillis();
		AccessibilityNodeInfo mNodeInfo = mService.getRootInActiveWindow();
		if (null == mNodeInfo) {
			isFinishRobEnvelope = true;
			Log.w(TAG, "handleDetailUI mNodeInfo is null");
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mNodeInfo.refresh();
		}
		AccessibilityNodeInfo fromNodeInfo = null;
		AccessibilityNodeInfo moneyNodeInfo = null;
		String fromName = null;
		Float money = 0.0f;
		long usedTime = openredEnvelopetime - onClickRedEnveloptime;
		String reciveTime = getDateTimeToStr(onClickRedEnveloptime);
		int QQVersion = getQQMobileVersion();
		String fromButtonId = QQConfig.getFromNameTextId(QQVersion);  //显示抢到谁的红包的text id
		String moneyButtonId = QQConfig.getMoneyTextId(QQVersion); //显示抢到的钱的text id
		
		fromNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, fromButtonId);
		if (null == fromNodeInfo) {
			fromNodeInfo = AccessibilityHelper.findNodeInfosByText(mNodeInfo, WechatConfig.WHO_ENVELOPE_KEY);
		}
		if (null != fromNodeInfo) {
			fromName = String.valueOf(fromNodeInfo.getText());
		}
		moneyNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, moneyButtonId);
		if (null != moneyNodeInfo) {
			String moneyStr = String.valueOf(moneyNodeInfo.getText());
			money = Float.valueOf(moneyStr);
		}
		
		if (isNewRedEnvelope && null != fromName && (money-0.0f) != 0) {
			isRobRedEnvelopeSuc = true;
			NotifyHelper.playSound(mContext);
			showReEnvelopeNotification(money);
			handleSaveData(fromName, money, usedTime, reciveTime);
		}
		isNewRedEnvelope = false;
		Log.i(TAG, "handleDetailUI name : " + fromName + "   money : " + money);
		
		Log.i(TAG, "isRobRedEnvelopeSuc : " + isRobRedEnvelopeSuc);
		if (isAutoReturn) { //for auto rob red envelope
			int autoAnswerType = Config.getConfig(mContext).getAutoAnswer();
			if (0 != autoAnswerType) {
				 new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
	                 @Override
	                 public void run() {
	                	 AccessibilityHelper.performBack(mService);
	                 }
	             }, Config.AUTO_ANSWSER_DELAY_TIME);
			} else {
				AccessibilityHelper.performBack(mService);
			}
		}
		isFinishRobEnvelope = true;
		isNewRedEnvelope = false;
		isDetailUi = true;
	}
	
	private String getDateTimeToStr(long time) {
		// TODO Auto-generated method stub
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = dateFormat.format(date);
		Log.d(TAG, "getDateTimeToStr time : " + time + " to String : " + dateStr);
		return dateStr;
	}
	
	private int getQQMobileVersion() {
	    PackageInfo mQQPackageInfo = null;
	    try {
	    	mQQPackageInfo = mContext.getPackageManager().getPackageInfo(QQConfig.QQ_PACKAGENAME, 0);
	    } catch (PackageManager.NameNotFoundException e) {
	         e.printStackTrace();
	    }
	    if(mQQPackageInfo == null) {
	         return 0;
	    }
	    Log.i(TAG, "versionCode : " + mQQPackageInfo.versionCode);
	    return mQQPackageInfo.versionCode;
	}
	
	private void showReEnvelopeNotification(float money) {
		// TODO Auto-generated method stub
		long when = System.currentTimeMillis();
		String text = mContext.getResources().getString(R.string.qq_red_envelope_notification, money);
		ComponentName component = new ComponentName(QQConfig.QQ_PACKAGENAME, QQConfig.QQ_RECORD_UI);
		Intent intent = new Intent();
		intent.setComponent(component);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			Context context = mContext.createPackageContext(QQConfig.QQ_PACKAGENAME, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			NotifyHelper.showNotify(mContext, text, when, pendingIntent);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "showReEnvelopeNotification : " + e.toString());
		}
	}
	
	private void handleSaveData(String fromName, float money, long uTime, String rTime) {
		// TODO Auto-generated method stub
		String name = fromName;
		String time = rTime;
		long usedTime = uTime;
		float m = money;
		RedEnvelopeDBHelper dbHelper = new RedEnvelopeDBHelper(mContext);
		ContentValues values = new ContentValues();
		values.put(Config.KEY_NAME, name);
		values.put(Config.KEY_MONEY, m);
		values.put(Config.KEY_USED_TIME, usedTime);
		values.put(Config.KEY_TIME, time);
		boolean isSuc = dbHelper.insertRecordTable(values);
		dbHelper.closeDB();
		if (isSuc) {
			Toast.makeText(mContext, mContext.getResources().getString(R.string.save_data_fail),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void handleChatListRedEnvelope(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		Log.d(TAG, "handleChatListRedEnvelope start");
		int mode = Config.getConfig(mContext).getRedPacketMode();
        if(mode == Config.RED_PACKET_MODE_3) { //Just notice patterns
        	isChatListReturn = true;
        	Log.w(TAG, "handleChatListRedEnvelope is RED_PACKET_MODE_3 return");
            return;
        }
		
		AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
	    if(nodeInfo == null) {
	    	isChatListReturn = true;
	        Log.w(TAG, "handleChatListRedEnvelope nodeInfo is null");
	        return;
	    }

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			nodeInfo.refresh();
		}

		if(mode != Config.RED_PACKET_MODE_0) {
	    	boolean isSingleChat = isSingleChatUi(nodeInfo);
	        if(mode == Config.RED_PACKET_MODE_1 && !isSingleChat) {//Filter group chat
	        	isChatListReturn = true;
	        	Log.w(TAG, "handleChatListRedEnvelope is filter group chat return");
	            return;
	        } else if(mode == Config.RED_PACKET_MODE_2 && isSingleChat) { //Filter single chat
	        	isChatListReturn = true;
	        	Log.w(TAG, "handleChatListRedEnvelope is filter single chat return");
	            return;
	        }
	    }
	    
	    if (!isChatListUI(nodeInfo)) {
	    	isChatListReturn = true;
	    	Log.w(TAG, "handleChatListRedEnvelope is not chat list return");
	    	return;
	    }

	    List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(QQConfig.QQ_RED_PACKET_TEXT);

	    if (null == list || list.isEmpty()) {
	    	isChatListReturn = true;
	        Log.w(TAG, "handleChatListRedEnvelope list is null or is empty return");
	        return;
	    }
	    
	    List<AccessibilityNodeInfo> tempList = new ArrayList<AccessibilityNodeInfo>();
	    int size = list.size();
	    for (int i=size-1; i>=0; i--) {
	    	AccessibilityNodeInfo node = list.get(i).getParent();
	    	Log.d(TAG, "handleChatListRedEnvelope node : " + node);
	    	if (null == node) {
	    		continue;
	    	}
	    	AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosByText(node, QQConfig.HAS_OPEN_TEXT);
	    	if (null != target) {
	    		Log.w(TAG, "has open red packet break");
	    		break;
	    	}
	    	
	    	target = AccessibilityHelper.findNodeInfosByText(node, QQConfig.CLICK_OPEN_TEXT);
	    	if (null == target) { //判断是否是口令红包
	    		target = AccessibilityHelper.findNodeInfosByText(node, QQConfig.PASSWORD_RED_PACKET_TEXT);
	    	}
	    	
	    	if (null != target) {
	    		boolean isSame = false;;
	    		for (int k=mNewNodeInfoList.size()-1; k>=0; k--) {
	    			if (node.equals(mNewNodeInfoList.get(k))) {
	    				isSame = true;
	    				break;
	    			}
	    		}
	    		
	    		if (!isSame) {
	    			tempList.add(node);
	    		}
	    	}
	    }
	    
	    int tempSize = tempList.size();
	    Log.i(TAG, "handleChatListRedEnvelope tempSize : " + tempSize);
	    if (tempSize > 0) {
	    	for (int i=tempSize-1; i>=0; i--) {
	    		mNewNodeInfoList.add(tempList.get(i));
	    	}
	    	
	    	int newSize = mNewNodeInfoList.size();
	    	clickNewNodeInfo(mNewNodeInfoList.get(newSize-1));
	    	mNewNodeInfoList.remove(newSize-1);
	    }
	      
	   Log.d(TAG, "handleChatListRedEnvelope end");
	}
	
	/**判断是否是聊天界面**/
	private boolean isChatListUI(AccessibilityNodeInfo nodeInfo) {
		if(nodeInfo == null) {
	         return false;
	    }
	        
	    int wechatVersion = getQQMobileVersion();
	    String id = QQConfig.getChatUiReturnBtnId(wechatVersion);
	        
	    AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
	    if (null == target) {
	    	target = AccessibilityHelper.findNodeInfosByText(nodeInfo, QQConfig.MESSAGE_KEY);
	    }
	    
	    if(target != null) {
	         return true;
	    }
	        
	   return false;
	}
	
	private boolean isSingleChatUi(AccessibilityNodeInfo nodeInfo) {
		// TODO Auto-generated method stub
		if(nodeInfo == null) {
	         return false;
	    }
	        
	    int wechatVersion = getQQMobileVersion();
	    String id = QQConfig.getChatListSubTitleId(wechatVersion);
	        
	    AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
	    if (null == target) {
	    	target = AccessibilityHelper.findNodeInfosByText(nodeInfo, QQConfig.ONLINE_TEXT);
	    }
	    
	    if (null == target) {
	    	id = QQConfig.getChatListTitleId(wechatVersion);
	    	target = AccessibilityHelper.findNodeInfosByText(nodeInfo, id);
	    }
	    
	    if(target != null) {
	         return true;
	    }
	        
	   return false;
	}
	
	private void clickNewNodeInfo(final AccessibilityNodeInfo node) {
		final String password = getRedPacketPassword(node);
		Log.i(TAG, "clickNewNodeInfo password : " + password);
		isFinishRobEnvelope = false;
		
		long sDelayTime = Config.getConfig(mContext).getOpenDelayTime();
        Log.i(TAG, "delay " + sDelayTime + "ms ");
        if(sDelayTime != 0) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                	if (null != password) { //口令红包
                		AccessibilityHelper.performClick(node);
            			handleAutoAnswer(password);
            			isLoadSuc = true;
            		} else { //非口令红包
            			isLoadSuc = AccessibilityHelper.performClick(node);
            		}
                }
            }, sDelayTime);
        } else {
        	if (null != password) { //口令红包
        		AccessibilityHelper.performClick(node);
    			handleAutoAnswer(password);
    			isLoadSuc = true;
    		} else { //非口令红包
    			isLoadSuc = AccessibilityHelper.performClick(node);
    		}
        }
        Log.i(TAG, "clickNewNodeInfo isLoadSuc : " + isLoadSuc);
        if (!isLoadSuc) {
        	isLoadSuc = AccessibilityHelper.performClick(node);
    	}
        
        if (!isLoadSuc) {
        	//Tear open red envelope failure
        	isFinishRobEnvelope = true;
        }
		
		isNewRedEnvelope = true;
		isAutoReturn = true;
    	onClickRedEnveloptime = System.currentTimeMillis();
        Config.getConfig(mContext).setAutoRobCount(); //automatic counts
	}
	
	/**获取口令红包口令**/
	private String getRedPacketPassword(AccessibilityNodeInfo nodeInfo) {
		String password = null;
		if (null == nodeInfo) {
			return password;
		}
		
		String desc = String.valueOf(nodeInfo.getContentDescription());
	    if(!"null".equals(desc) && null != desc 
	    		&& desc.contains(QQConfig.PASSWORD_TEXT)) {
	    	String []descArray = desc.split(",");
	        password = descArray[0].replace(QQConfig.PASSWORD_TEXT, "");
	        return password;
	    }
	        
		AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosByText(nodeInfo, QQConfig.PASSWORD_RED_PACKET_TEXT);
	    if (null == target) {
	    	return password;
	    }
	    
	    target =  target.getParent();
	    if (null != target) {
		    desc = String.valueOf(target.getContentDescription());
		    if(!"null".equals(desc) && null != desc 
		    		&& desc.contains(QQConfig.PASSWORD_TEXT)) {
		    	String []descArray = desc.split(",");
		        password = descArray[0].replace(QQConfig.PASSWORD_TEXT, "");
		        return password;
		    }
	        
		    if( target.getChildCount() >= 2) {
		         AccessibilityNodeInfo node = target.getChild(1);
		         for (int i=0; i<node.getChildCount()-1; i++) {
			         if(QQConfig.TEXTVIEW_CLASS_NAME.equals(node.getClassName())) {
			        	 String textStr = String.valueOf(node.getText());
			        	 if(!"null".equals(textStr) && null != textStr 
			     	    		&& textStr.contains(QQConfig.PASSWORD_TEXT)) {
			     	    	String []descArray = textStr.split(",");
			     	        password = descArray[0].replace(QQConfig.PASSWORD_TEXT, "");
			     	        return password;
			     	    }
			         }
		         }
		    }
	    }
		return password;
		
	}
	
	/**自动回复**/
	private void handleAutoAnswer(CharSequence answerStr) {
		// TODO Auto-generated method stub
		AccessibilityNodeInfo mNodeInfo = mService.getRootInActiveWindow();
		if (null == mNodeInfo) {
			Log.w(TAG, "handleRedEnvelopData mNodeInfo is null");
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mNodeInfo.refresh();
		}
		AccessibilityNodeInfo textNodeInfo = null;
		AccessibilityNodeInfo sendNodeInfo = null;
		int qqVersion = getQQMobileVersion();
		String textId = QQConfig.getQQMobileEditTextId(qqVersion);
		String sendButtonId = QQConfig.getSendButtonId(qqVersion);
		
		try {
			textNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, textId);
			if (null != textNodeInfo) {
				Bundle arguments = new Bundle();
//				arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, answerStr);
//				AccessibilityHelper.performAction(textNodeInfo, arguments);
			}
			Log.d(TAG, "handleAutoAnswer textNodeInfo : " + textNodeInfo);
			
			sendNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, sendButtonId);
			if (null == sendNodeInfo) {
				sendNodeInfo = AccessibilityHelper.findNodeInfosByText(mNodeInfo, QQConfig.SEND_BUTTON_TEXT);
			}
			if (null != sendNodeInfo) {
				AccessibilityHelper.performClick(sendNodeInfo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "handleAutoAnswer exception : " + e.toString());
		}
	}
	
	private CharSequence getAutoAnswerStr() {
		CharSequence str = null;
		int autoType = Config.getConfig(mContext).getAutoAnswer();
		final CharSequence[] entries = mContext.getResources()
				.getTextArray(R.array.auto_answer_entries);
		if (entries.length >= autoType && 0 != autoType) {
			str = entries[autoType];
		}
		return str;
	}
}
