package com.android.redenvelope;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.android.redenvelope.database.RedEnvelopeDBHelper;
import com.android.redenvelope.service.RedEnvelopeService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WechatAccessiblity {
	private static final String TAG = "WechatAccessibility";
	private RedEnvelopeService mService;
	private Context mContext;
	private boolean isNotificationRedEnvelope = false;
	private static final int WINDOW_NONE = 0;
    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
    private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
    private static final int WINDOW_LAUNCHER = 3;
    private static final int WINDOW_OTHER = -1;
    private int mCurrentWindow = WINDOW_NONE;
    private boolean isRobRedEnvelopeSuc = false;
    private boolean isNewRedEnvelope = false;
    private long openredEnvelopetime = 0;   //拆開红包的时间
    private long onClickRedEnveloptime = 0; //在信息列表点击红包的时间
    private boolean isLoadSuc = false; //判断拆红包是否成功
    private boolean isNeedReturnHome = false;
    private boolean isLuckyMoneyReceiveUI = false; //for determine whether to return to the home
	private boolean isFinishRobEnvelope = true; //判断抢红包是否已经完成
	private boolean isNeedToDeal = false;  //是否还有没处理完的红包
	private boolean isAutoReturn = false;  //自动打开的红包是否自动返回
	private boolean isChatListReturn = false;
	private int listViewItemCount = 0; //listview 来信息前的item的总数
	//start by guojun topwise fix bug#9650
	private String mCurrentClassName = null;
	//end by guojun topwise fix bug#9650
	//未抢节点列表
    private ArrayList<AccessibilityNodeInfo> mNewNodeInfoList = new ArrayList<AccessibilityNodeInfo>();
	private ArrayList<PendingIntent> mPendingIntentList = new ArrayList<PendingIntent>();
    //保存每个聊天列表中的总的聊天记录总数
	private Map<String, Object> recordItemCount = new HashMap<String, Object>();
	//start, add by zl topwise for bug[10219]
	public Handler mRedHandle;
	private boolean isCheckTooSlow = false;
	//end, add by zl topwise for bug[10219]
	private boolean isClickNodeInfo = false;//判断是否已经点击来节点
	//start by guojun topwise // FIXME: 16-9-3 bug#11058
	private String mLastEventType = null;
	private String mThisEventType = null;
	private boolean isLastEventisClickEvent = false;
	private long mLastEventTime = 0;
	private long mThisEventTime = 0;
	//start by guojun topwise // FIXME: 16-9-3 bug#11058
	public WechatAccessiblity(RedEnvelopeService service, Context context) {
		// TODO Auto-generated constructor stub
		mService = service;
		mContext = context;

		//start by huanggq for mm video
		mmVideoManager = new MMVideoManager(context);
		mRedHandle = new Handler(Looper.getMainLooper());//add by zl topwise for bug[10219]
	}
	
	public void onReceiveAccessibilityEvent(AccessibilityEvent event, boolean isEnable) {

		final int eventType = event.getEventType();
		
		Log.i(TAG, "eventTypeStr = " + AccessibilityEvent.eventTypeToString(eventType));
		check();
		//start by guojun topwise // FIXME: 16-9-3 bug#11058
		if (AccessibilityEvent.eventTypeToString(eventType).equals("TYPE_VIEW_CLICKED")
				|| AccessibilityEvent.eventTypeToString(eventType).equals("TYPE_VIEW_SCROLLED")
				|| AccessibilityEvent.eventTypeToString(eventType).equals("TYPE_WINDOW_STATE_CHANGED")){
			if (mLastEventTime == 0 || mThisEventTime == 0){
				mLastEventTime = System.currentTimeMillis();
				mThisEventTime = System.currentTimeMillis();
			}else {
				mLastEventTime = mThisEventTime;
				mThisEventTime = System.currentTimeMillis();
			}
			if (mLastEventType == null || mThisEventType == null) {
				mLastEventType = AccessibilityEvent.eventTypeToString(eventType);
				mThisEventType = AccessibilityEvent.eventTypeToString(eventType);
			} else /*if (mLastEventType != null && mThisEventType != null)*/ {
				mLastEventType = mThisEventType;
				mThisEventType = AccessibilityEvent.eventTypeToString(eventType);
				if (mLastEventType.equals("TYPE_VIEW_CLICKED") && mThisEventType.equals("TYPE_VIEW_SCROLLED")
						&& (mThisEventTime - mLastEventTime) < 500){
					isLastEventisClickEvent = true;
				}
			}
		}
		Log.d(TAG,"mLastEventType : " +mLastEventType +"    mThisEventType : "+mThisEventType);
		Log.d(TAG,"mLastEventTime : " +mLastEventTime +"    mThisEventTime : "+mThisEventTime);
		//start by guojun topwise // FIXME: 16-9-3 bug#11058

		if (!isEnable) {
			switch (eventType) {
				case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
					handleWindowStateChange(event);
					break;
				case AccessibilityEvent.TYPE_VIEW_CLICKED:
					handleClickEvent(event);
					break;
				default:
					break;
			}

			return;
		}
		//end by huanggq for mm video

		switch (eventType) {
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: //64
				notificationEvent(event);
				break;
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //32
				openRedEnvelope(event);
				break;
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  //2048
				//Is not in the chat interface or chat list, don't deal with it
				if (mCurrentWindow != WINDOW_LAUNCHER) {
					return;
				}
				if (isNotificationRedEnvelope) {
					handleChatListRedEnvelope();
				}
				break;
			case AccessibilityEvent.TYPE_VIEW_SCROLLED: //4096
				//start by guojun topwise // FIXME: 16-9-3 bug#11058
				if (isLastEventisClickEvent){
					Log.d(TAG,"last event is click event ,so return");
					isLastEventisClickEvent = false;
					return;
				}
				//end by guojun topwise // FIXME: 16-9-3 bug#11058
				handleChatListNewRedEnvelope(event);
				break;
			//start by huanggq for mm video
			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				handleClickEvent(event);
				break;
			//end by huanggq for mm video
			default:
				break;
		}
	}

	private void handleChatListNewRedEnvelope(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		Log.d(TAG, "handleChatListNewRedEnvelope");
		int mode = Config.getConfig(mContext).getRedPacketMode();
		Log.d(TAG, "handleChatListNewRedEnvelope mode : " + mode);
        if(mode == Config.RED_PACKET_MODE_3) { //Just notice patterns
            return;
        }

        AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "handleChatListNewRedEnvelope nodeInfo is null");
            return;
        }
		//start by guojun // FIXME: 16-9-8 #11566
		if (!event.getClassName().equals(WechatConfig.LISTVIEW_CLASS_NAME)){
			Log.w(TAG,"not a redenvelope event ,so return");
			return;
		}
		//end by guojun // FIXME: 16-9-8 #11566
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			nodeInfo.refresh();
		}
		if(mode != Config.RED_PACKET_MODE_0) {
            boolean isMember = isMemberChatUi(nodeInfo);
            Log.i(TAG, "handleChatListNewRedEnvelope isMember : " + isMember);
            if(mode == Config.RED_PACKET_MODE_1 && isMember) {//Filter group chat
                return;
            } else if(mode == Config.RED_PACKET_MODE_2 && !isMember) { //Filter single chat
                return;
            }
        }

        String titleName = getChatListTitle(nodeInfo);
        if (titleName.isEmpty() || "null".equals(titleName)) { //非聊天界面
        	listViewItemCount = 0;
        	Log.i(TAG, "handleChatListNewRedEnvelope titleName.isEmpty() return");
			return;
		}
        
        if (isNotificationRedEnvelope) {
        	int count = event.getItemCount();
        	if (count > 0) {
	        	listViewItemCount = count;
	        	updateRecordItemCount(titleName, listViewItemCount);
        	}
			return;
		}
        
        int addItemCount = 0;
        int itemCount = event.getItemCount();
        int toIndex = event.getToIndex();
        int fromIndex = event.getFromIndex();
        int recordCount = getRecordItemCount(titleName);
		//start by guojun topwise fix bug#9650
		if (0 != recordCount) {
			listViewItemCount = recordCount;
		}
		//end by guojun topwise fix bug#9650
        Log.i(TAG, "fromIndex : " + fromIndex + "  toIndex : " + toIndex);
        Log.i(TAG, "itemCount : " + itemCount + " listViewItemCount : " + listViewItemCount + "  recordCount : " + recordCount);
        if (0 == fromIndex && 1 == toIndex /*&& 0 == recordCount*/) { //for first time in and no messages
        	/*if (isNewLastRedEnvelopeNode(nodeInfo)) {
        		listViewItemCount = 1;
        	} else {
        		listViewItemCount = 1;
        		updateRecordItemCount(titleName, listViewItemCount);
        		return;
        	}*/
			listViewItemCount = 1;
		} else if ((0 >= listViewItemCount) || toIndex < (itemCount -1)
				|| listViewItemCount > itemCount) {
			if (itemCount > 0)
				listViewItemCount = itemCount;
			updateRecordItemCount(titleName, listViewItemCount);
			Log.i(TAG, "handleChatListNewRedEnvelope listViewItemCount : " + listViewItemCount);
			Log.i(TAG, "not in chat list end or not in chat list return" );
			return; //do nothing to enter the chat list for the first time or not in the end of chat list 
		} 
        
		addItemCount = itemCount - listViewItemCount;
		listViewItemCount = itemCount;
		updateRecordItemCount(titleName, listViewItemCount);
		
        AccessibilityNodeInfo envelopeNodeInfo = AccessibilityHelper.findNodeInfosByText(nodeInfo, WechatConfig.ENVELOPE_TEXT_KEY_OTHER);
        if (null == envelopeNodeInfo) {
        	envelopeNodeInfo = AccessibilityHelper.findNodeInfosByText(nodeInfo, WechatConfig.ENVELOPE_TEXT_KEY);
        }
        
        AccessibilityNodeInfo listRootNodeInfo = getListViewRootNodeInfo(envelopeNodeInfo);
        if (null != listRootNodeInfo && listRootNodeInfo.getChildCount() > 0
        		&& addItemCount > 0 ) {
        	int listCount = listRootNodeInfo.getChildCount();
        	Log.i(TAG, "listRootNodeInfo.getChildCount : " + listRootNodeInfo.getChildCount());
        	if (addItemCount > listCount) { //only obtain the information items of the current window
        		addItemCount = listCount;
        	}
        	
        	List<AccessibilityNodeInfo> tempNewNodeList = new ArrayList<AccessibilityNodeInfo>();
        	for (int i=1; i<=addItemCount; i++) {
		        AccessibilityNodeInfo lastNodeInfo = listRootNodeInfo.getChild(listRootNodeInfo.getChildCount()-i);
		        if (null == lastNodeInfo) {
		        	break;
		        }
		        
		        String lastNodeText = getMessageFromNode(lastNodeInfo);
		        Log.i(TAG, "last nodeInfo text : " + lastNodeText);
				if (lastNodeText.contains(WechatConfig.HAS_OPEN_MSG_TEXT)) {
		        	Log.i(TAG, "handleChatListNewRedEnvelope last nodeInfo break");
		        	break;
		        }
		       
		        AccessibilityNodeInfo newNodeInfo = AccessibilityHelper.findNodeInfosByText(lastNodeInfo, WechatConfig.ENVELOPE_TEXT_KEY_OTHER);
		        if (null == newNodeInfo) {
		        	newNodeInfo = AccessibilityHelper.findNodeInfosByText(lastNodeInfo, WechatConfig.ENVELOPE_TEXT_KEY);
		        	if (null != newNodeInfo) {
			        	String  contentDes = (String) newNodeInfo.getContentDescription();
				        if (!isRealRedPacket(contentDes)) {
				        	continue;
				        }
		        	}
		        }
		        Log.i(TAG, "handleChatListNewRedEnvelope newNodeInfo : " + newNodeInfo);
		        
		        Log.i(TAG, "isFinishRobEnvelope : " + isFinishRobEnvelope);
		        if (null != newNodeInfo) {
			        if (isFinishRobEnvelope && 1 == addItemCount) {
			        	clickNewNodeInfo(newNodeInfo);
			        } else {
			        	tempNewNodeList.add(newNodeInfo);
			        }
		        } else {
		        	continue;
		        }
        	}
        	if (!tempNewNodeList.isEmpty()) {
	        	int size = tempNewNodeList.size();
	        	for (int i=size-1; i>=0; i--) {
	        		mNewNodeInfoList.add(tempNewNodeList.get(i));
	        	}
	        	
	        	if (isFinishRobEnvelope && addItemCount > 1) {
	        		int count = mNewNodeInfoList.size();
	        		clickNewNodeInfo(mNewNodeInfoList.get(count-1));
	        		mNewNodeInfoList.remove(count-1);
	        	}
	        }
        }
        
	}
	
	private AccessibilityNodeInfo getListViewRootNodeInfo(AccessibilityNodeInfo nodeInfo) {
		// TODO Auto-generated method stub
		Log.i(TAG, "getListViewRootNodeInfo");
		if (null == nodeInfo) {
			return null;
		}
		nodeInfo = nodeInfo.getParent();
		if (null == nodeInfo) {
			return null;
		}
		nodeInfo = nodeInfo.getParent();
		if (null == nodeInfo) {
			return null;
		}
		nodeInfo = nodeInfo.getParent();
		if (null != nodeInfo) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				nodeInfo.refresh();
			}
		}
		return nodeInfo;
	}

	private void clickNewNodeInfo(AccessibilityNodeInfo node) {
		boolean isSuc = AccessibilityHelper.performClick(node);
		if (isSuc) {
			Log.i(TAG, "clickNewNodeInfo isFinishRobEnvelope : " + isFinishRobEnvelope);
			onClickRedEnveloptime = System.currentTimeMillis();
//	        isNotificationRedEnvelope = false;
			isFinishRobEnvelope = false;
			isAutoReturn = true;
			isClickNodeInfo = true;
			Config.getConfig(mContext).setAutoRobCount(); //automatic counts
		} else {
			isChatListReturn = true;
			isNotificationRedEnvelope = false;
			isFinishRobEnvelope = true;
			isAutoReturn = false;
        	Log.i(TAG, "onload fail to return");
        	Config.getConfig(mContext).setAutoRobCount(); //automatic counts
        	return;
		}
	}
	
	private void notificationEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		Parcelable data =  event.getParcelableData();
		if (null == data || !(data instanceof Notification)) {
			Log.i(TAG, "notifcationEvent return");
			return;
		}
		
		List<CharSequence> mText = event.getText();
		if (!mText.isEmpty()) {
			for (CharSequence c : mText) {
                String str = String.valueOf(c);
                Log.i(TAG, "notificationEvent str = " + str);
                if (isWechatRedPacket(str)) {
                    openNotification((Notification)data);
                    break;
                }
            }
		}
	}
	
	private boolean isWechatRedPacket(String str) {
		if (!str.contains(WechatConfig.ENVELOPE_TEXT_KEY)) {
			Log.i(TAG, str + "is not contains " + WechatConfig.ENVELOPE_TEXT_KEY);
			return false;
		}
		String []strArray = str.split("]");
		if (strArray.length < 2) {
			Log.i(TAG, "isWechatRedPacket strArray.length < 2");
			return false;
		}
		if (strArray[1].isEmpty()) {
			Log.i(TAG, "isWechatRedPacket strArray[1].isEmpty");
			return false;
		}
		return true;
	}
	
	public void openNotification(Notification notification) {
		// TODO Auto-generated method stub
		PendingIntent mPendingIntent = notification.contentIntent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (!WechatConfig.WECHAT_PACKAGENAME.equals(mPendingIntent.getCreatorPackage())) {
                Log.i(TAG, "openNotification return");
                return;
            }
		}

		//The current rob red envelope haven't finished
		if (!isFinishRobEnvelope || isNotificationRedEnvelope) {
			mPendingIntentList.add(mPendingIntent);
			Log.d(TAG, "openNotification isFinishRobEnvelope : " + isFinishRobEnvelope + " isNotificationRedEnvelope : "
				+ isNotificationRedEnvelope);
			Log.i(TAG, "openNotification not finish return");
			return;
		}
		
		isNotificationRedEnvelope = true;
		mRedHandle.removeCallbacks(mClickBack);
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
		// TODO Auto-generated method stub
		String classNmae = String.valueOf(event.getClassName());
		//start by guojun topwise fix bug#9650
		mCurrentClassName = classNmae;
		//end by guojun topwise fix bug#9650
		Log.i(TAG, "openRedEnvelope class name : " + classNmae);
		if (WechatConfig.WECHAT_RECEIVE_UI.equals(classNmae)) {
			mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
			handleLuckyMoneyReceive();
			isLuckyMoneyReceiveUI = true;
		} else if (WechatConfig.WECHAT_LAUNCHER_UI.equals(classNmae) || WechatConfig.WECHAT_CHATTING_UI.equals(classNmae)) {
			mCurrentWindow = WINDOW_LAUNCHER;
			resetRecordItemCount(event);//add by zl topwise for bug[10219]
			handleLauncheUI();
		} else if (WechatConfig.WECHAT_DETAIL_UI.equals(classNmae)) {
			mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
			handleDetailUI();
		} else {
			mCurrentWindow = WINDOW_OTHER;
		}

		//start by huanggq for mm video
		handleWindowStateChange(event);
		//end by huanggq for mm video

	}
	
	private void handleLauncheUI() {
//		isAutoReturn = false;
		mRedHandle.removeCallbacks(mCheckIsTooSlow);//add by zl topwise for bug[10219]
		if (isClickNodeInfo) {
			isFinishRobEnvelope = true;
			isNotificationRedEnvelope = false;
		}
		handleChatListRedEnvelope();
		//for auto answer
		int autoAnswerType = Config.getConfig(mContext).getAutoAnswer();
		if (0 != autoAnswerType && isRobRedEnvelopeSuc && isAutoReturn) {
			handleAutoAnswer();
			isAutoReturn = false;
		} else if (0 == autoAnswerType && isFinishRobEnvelope) {
			isAutoReturn = false;
		}
		isRobRedEnvelopeSuc = false;
		
		if (isFinishRobEnvelope) {
			int newNodeListSize = mNewNodeInfoList.size();
			if (newNodeListSize > 0) {
				AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
		        if(null != nodeInfo) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
						nodeInfo.refresh();
					}
					String titleName = getChatListTitle(nodeInfo);
					Log.i(TAG, "handleChatListNewRedEnvelope titleName : " + titleName.isEmpty());
			        if (!titleName.isEmpty() || "null".equals(titleName)) { //非聊天界面
						clickNewNodeInfo(mNewNodeInfoList.get(newNodeListSize-1));
						mNewNodeInfoList.remove(newNodeListSize-1);
			        }
		        }
			} else {
				int size = mPendingIntentList.size();

				if (0 != size) {
					NotifyHelper.send(mPendingIntentList.get(size-1));
					mPendingIntentList.remove(size-1);
					isNeedToDeal = true;
				} else {
					isNeedToDeal = false;
				}
			}
		}
		
		//for determine whether to return to the home and lock screen
		Log.i(TAG, "---------------isLuckyMoneyReceiveUI : " + isLuckyMoneyReceiveUI  + "isChatListReturn" + isChatListReturn
				+ "  isNeedReturnHome : " + isNeedReturnHome + "  isNeedToDeal : " + isNeedToDeal);
		if ((isLuckyMoneyReceiveUI && isNeedReturnHome && !isNeedToDeal)
				|| (isNeedReturnHome && isChatListReturn)) {
			AccessibilityHelper.performBack(mService);
			AccessibilityHelper.performHome(mService);
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                	handleKeyguardLock();
                }
            }, Config.LOCK_KEYGUARD_DELAY_TIME);
			isNeedReturnHome = false;
			isClickNodeInfo = false;
		}
		if (isClickNodeInfo && isNeedReturnHome && !isNeedToDeal && isFinishRobEnvelope) {
			AccessibilityHelper.performBack(mService);
			AccessibilityHelper.performHome(mService);
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					handleKeyguardLock();
				}
			}, Config.LOCK_KEYGUARD_DELAY_TIME);
			isClickNodeInfo = false;
		}
		isChatListReturn = false;
		isLuckyMoneyReceiveUI = false;
		isNewRedEnvelope = false;
		/*isFinishRobEnvelope = true;
		isNotificationRedEnvelope = false;*/
	}
	
	private void handleKeyguardLock () {
		Intent intent = new Intent();
		intent.setAction(Config.ACTION_KEYGUARD_LOCK);
		mContext.sendBroadcast(intent);
	}
	
	private void handleAutoAnswer() {
		// TODO Auto-generated method stub
		AccessibilityNodeInfo mNodeInfo = mService.getRootInActiveWindow();
		if (null == mNodeInfo) {
			Log.i(TAG, "handleRedEnvelopData mNodeInfo is null");
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mNodeInfo.refresh();
		}
		AccessibilityNodeInfo textNodeInfo = null;
		AccessibilityNodeInfo sendNodeInfo = null;
		int wechatVersion = getWechatVersion();
		String textId = WechatConfig.getWechatEditTextId(wechatVersion);
		String sendButtonId = WechatConfig.getSendButtonId(wechatVersion);
		
		try {
			if (textId != null){
				textNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, textId);
			}
			if (null == textNodeInfo) {
				textNodeInfo = getMsgEditTextNodeInfo(mNodeInfo);
			}
			
			if (null != textNodeInfo) {
				Bundle arguments = new Bundle();
				CharSequence str = null;
				int autoType = Config.getConfig(mContext).getAutoAnswer();
				final CharSequence[] entries = mContext.getResources()
						.getTextArray(R.array.auto_answer_entries);
				if (autoType == entries.length-1) {
					str = Config.getConfig(mContext).getDefinedAutoReply();
				} else if (entries.length >= autoType && 0 != autoType) {
					str = entries[autoType];
				}
//				arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, str);
//				AccessibilityHelper.performAction(textNodeInfo, arguments);
			}
			Log.d(TAG, "handleAutoAnswer textNodeInfo : " + textNodeInfo);
			if (sendButtonId != null){
				sendNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, sendButtonId);
			}
			if (null == sendNodeInfo) {
				sendNodeInfo = AccessibilityHelper.findNodeInfosByText(mNodeInfo, WechatConfig.SEND_KEY);
			}
			if (null != textNodeInfo && null != sendNodeInfo) {
				AccessibilityHelper.performClick(sendNodeInfo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "handleAutoAnswer exception : " + e.toString());
		}
		Log.d(TAG, "handleAutoAnswer sendNodeInfo : " + sendNodeInfo);
	}

	private void handleDetailUI() {
		// TODO Auto-generated method stub
		//start, add by zl topwise for bug[10219]
		isCheckTooSlow = false;
		mRedHandle.removeCallbacks(mCheckIsTooSlow);
		//end, add by zl topwise for bug[10219]
		isClickNodeInfo = false;
		if (true == isNewRedEnvelope) {
			openredEnvelopetime = System.currentTimeMillis();
		}
		
		AccessibilityNodeInfo mNodeInfo = mService.getRootInActiveWindow();
		if (null == mNodeInfo) {
			isFinishRobEnvelope = true;
			isNotificationRedEnvelope = false;
			isAutoReturn = false;
			isRobRedEnvelopeSuc = false;
			Log.d(TAG, "handleDetailUI mNodeInfo is null");
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
		int wechatVersion = getWechatVersion();
		String fromButtonId = WechatConfig.getFromNameTextId(wechatVersion);  //显示抢到谁的红包的text id
		String moneyButtonId = WechatConfig.getMoneyTextId(wechatVersion); //显示抢到的钱的text id

		if (fromButtonId != null){
			fromNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, fromButtonId);
		}
		if (null == fromNodeInfo) {
			fromNodeInfo = AccessibilityHelper.findNodeInfosByText(mNodeInfo, WechatConfig.WHO_ENVELOPE_KEY);
		}
		if (null != fromNodeInfo) {
			fromName = String.valueOf(fromNodeInfo.getText());
		}
		if (moneyButtonId != null){
			moneyNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, moneyButtonId);
		}
		if (null == moneyNodeInfo) {
			moneyNodeInfo = AccessibilityHelper.findNodeInfosByText(mNodeInfo, WechatConfig.MONEY_TEXT_KEY);
		}
		if (null != moneyNodeInfo) {
			try {
				String moneyStr = String.valueOf(moneyNodeInfo.getText());
				money = Float.valueOf(moneyStr);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (isAutoReturn && isNewRedEnvelope && (money-0.0f) != 0) {
			isRobRedEnvelopeSuc = true;
			NotifyHelper.playSound(mContext);
//			showReEnvelopeNotification(money);
			if (null == fromName || "null".equals(fromName)|| (null != fromName && fromName.isEmpty()))
				fromName = mContext.getResources().getString(R.string.wechat_redpackage_name);
			handleSaveData(fromName, money, usedTime, reciveTime);
		}
		isNewRedEnvelope = false;
		Log.i(TAG, "handleDetailUI name : " + fromName + "   money : " + money);
		
		Log.i(TAG, "isRobRedEnvelopeSuc : " + isRobRedEnvelopeSuc);
		if (isAutoReturn) { //for auto rob red envelope
			int autoAnswerType = Config.getConfig(mContext).getAutoAnswer();
			if (0 != autoAnswerType) {
				 mRedHandle.postDelayed(mClickBack,Config.AUTO_ANSWSER_DELAY_TIME);
			} else {
				AccessibilityHelper.performBack(mService);
			}
		}
		isNotificationRedEnvelope = false;
		isFinishRobEnvelope = true;
	}

	Runnable mClickBack = new Runnable() {
		@Override
		public void run() {
			AccessibilityHelper.performBack(mService);
		}
	};

	private String getDateTimeToStr(long time) {
		// TODO Auto-generated method stub
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = dateFormat.format(date);
		Log.d(TAG, "getDateTimeToStr time : " + time + " to String : " + dateStr);
		return dateStr;
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

	private void showReEnvelopeNotification(float money) {
		// TODO Auto-generated method stub
		long when = System.currentTimeMillis();
		String text = mContext.getResources().getString(R.string.red_envelope_notification, money);
		ComponentName component = new ComponentName(WechatConfig.WECHAT_PACKAGENAME, WechatConfig.WECHAT_RECORD_UI);
		Intent intent = new Intent();
		intent.setComponent(component);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			Context context = mContext.createPackageContext(WechatConfig.WECHAT_PACKAGENAME, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			NotifyHelper.showNotify(mContext, text, when, pendingIntent);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "showReEnvelopeNotification : " + e.toString());
		}
	}

	public void handleLuckyMoneyReceive() {
		// TODO Auto-generated method stub
		AccessibilityNodeInfo mNodeInfo = mService.getRootInActiveWindow();
		isClickNodeInfo = false;
		if (null == mNodeInfo) {
			isFinishRobEnvelope = true;
			isNotificationRedEnvelope = false;
			isAutoReturn = false;
			Log.d(TAG, "handleLuckyMoneyReceive mNodeInfo is null");
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mNodeInfo.refresh();
		}
		AccessibilityNodeInfo targetNode = null;
		int wechatVersion = getWechatVersion();
		if (wechatVersion < WechatConfig.USE_ID_MIN_VERSION) {
            targetNode = AccessibilityHelper.findNodeInfosByText(mNodeInfo, WechatConfig.OPEN_ENVELOPE_KEY);
        } else {
            String buttonId = WechatConfig.getOpenButtonId(wechatVersion);

            if(buttonId != null) {
                targetNode = AccessibilityHelper.findNodeInfosById(mNodeInfo, buttonId);
            }

            if(targetNode == null) {
                //分别对应固定金额的红包 拼手气红包
                AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(mNodeInfo, 
                		WechatConfig.MONEY_RECEIVE_TEXT1, WechatConfig.MONEY_RECEIVE_TEXT2, WechatConfig.MONEY_RECEIVE_TEXT3);

                if(textNode != null) {
                    for (int i = 0; i < textNode.getChildCount(); i++) {
                        AccessibilityNodeInfo node = textNode.getChild(i);
                        if (WechatConfig.BUTTON_CLASS_NAME.equals(node.getClassName())) {
                            targetNode = node;
                            break;
                        }
                    }
                }
            }

            if(targetNode == null) { //通过组件查找
                targetNode = AccessibilityHelper.findNodeInfosByClassName(mNodeInfo, WechatConfig.BUTTON_CLASS_NAME);
            }
        }

		if(targetNode != null) {
            final AccessibilityNodeInfo n = targetNode;
            long sDelayTime = Config.getConfig(mContext).getOpenDelayTime();
			if (Config.SUPPORT_DELAY_TIME_RANDOM && (sDelayTime == 3)) {
				sDelayTime = getRandomDelayTime() * 1000; //ms
			}
            Log.i(TAG, "delay " + sDelayTime + "ms ");
			boolean isDelayOpen = false;
            if(sDelayTime != 0) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    	isLoadSuc = AccessibilityHelper.performClick(n);
						if (!isLoadSuc) {
							isLoadSuc = AccessibilityHelper.performClick(n);
						}

						if (isLoadSuc) {
//							isRobRedEnvelopeSuc = true;
							isClickNodeInfo = true;
							mRedHandle.postDelayed(mCheckIsTooSlow, Config.CHECK_IS_TOO_SLOW_DELAY_TIME);//add by zl topwise for bug[10219]
						} else {
							//Tear open red envelope failure
							isFinishRobEnvelope = true;
							isNotificationRedEnvelope = false;
						}
						Log.i(TAG, "---------- isLoadSuc : " + isLoadSuc);
						isNewRedEnvelope = true;
						isLoadSuc = false;
                    }
                }, sDelayTime);
				isDelayOpen = true;
            } else {
            	isLoadSuc = AccessibilityHelper.performClick(n);
            }
            Log.i(TAG, "handleLuckyMoneyReceive isLoadSuc : " + isLoadSuc);
			if (!isDelayOpen) {
				if (!isLoadSuc) {
					isLoadSuc = AccessibilityHelper.performClick(n);
				}

				if (isLoadSuc) {
//					isRobRedEnvelopeSuc = true;
					isClickNodeInfo = true;
					mRedHandle.postDelayed(mCheckIsTooSlow, Config.CHECK_IS_TOO_SLOW_DELAY_TIME);//add by zl topwise for bug[10219]
				} else {
					//Tear open red envelope failure
					isFinishRobEnvelope = true;
					isNotificationRedEnvelope = false;
				}
				isNewRedEnvelope = true;
				isLoadSuc = false;
			}
        } else { //手慢了
        	if (isAutoReturn)
        		AccessibilityHelper.performBack(mService);  //return
        	isFinishRobEnvelope = true;
			isNotificationRedEnvelope = false;
			isAutoReturn = false;
        }
	}
	
	private void handleChatListRedEnvelope() {
		// TODO Auto-generated method stub
		Log.d(TAG, "handleChatListRedEnvelope");
		//When single chat send a red envelope will be returned many times
		if (!isNotificationRedEnvelope) {
        	return;
        }
		
		int mode = Config.getConfig(mContext).getRedPacketMode();
		Log.d(TAG, "handleChatListRedEnvelope mode : " + mode);
        if(mode == Config.RED_PACKET_MODE_3) { //Just notice patterns
            return;
        }

        AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "handleChatListRedEnvelope nodeInfo is null");
            return;
        }

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			nodeInfo.refresh();
		}
		if(mode != Config.RED_PACKET_MODE_0) {
            boolean isMember = isMemberChatUi(nodeInfo);
            Log.d(TAG, "handleChatListRedEnvelope isMember : " + isMember);
            if(mode == Config.RED_PACKET_MODE_1 && isMember) {//Filter group chat
            	isChatListReturn = true;
            	isNotificationRedEnvelope = false;
                return;
            } else if(mode == Config.RED_PACKET_MODE_2 && !isMember) { //Filter single chat
            	isChatListReturn = true;
            	isNotificationRedEnvelope = false;
                return;
            }
        }

        String titleName = getChatListTitle(nodeInfo);
        if (null == titleName || titleName.isEmpty() || "null".equals(titleName)) { //非聊天界面
        	listViewItemCount = 0;
			isNotificationRedEnvelope = false;
        	Log.w(TAG, "handleChatListRedEnvelope titleName.isEmpty() return");
			return;
		}
        
        AccessibilityNodeInfo envelopeNodeInfo = AccessibilityHelper.findNodeInfosByText(nodeInfo, WechatConfig.ENVELOPE_TEXT_KEY_OTHER);
        if (null == envelopeNodeInfo) {
        	envelopeNodeInfo = AccessibilityHelper.findNodeInfosByText(nodeInfo, WechatConfig.ENVELOPE_TEXT_KEY);
			//start, add by zl topwise for bug[10066]
			if (null == envelopeNodeInfo) {
				Log.i(TAG, "handleChatListRedEnvelope envelopeNodeInfo is null to return");
				isNotificationRedEnvelope = false;
				return;
			}
			//end, add by zl topwise for bug[10066]
        	String  contentDes = (String) envelopeNodeInfo.getContentDescription();
	        if (!isRealRedPacket(contentDes)) {
				isNotificationRedEnvelope = false;
	        	return;
	        }
        }
        
        AccessibilityNodeInfo listRootNodeInfo = getListViewRootNodeInfo(envelopeNodeInfo);
        if (null != listRootNodeInfo && listRootNodeInfo.getChildCount() > 0) {
        	Log.d(TAG, "listRootNodeInfo.getChildCount : " + listRootNodeInfo.getChildCount());
        	
		    AccessibilityNodeInfo lastNodeInfo = listRootNodeInfo.getChild(listRootNodeInfo.getChildCount()-1);
		    if (null == lastNodeInfo) {
				isNotificationRedEnvelope = false;
		        return;
		    }
		    Log.i(TAG, "handleChatListRedEnvelope last nodeInfo text : " + getMessageFromNode(lastNodeInfo));
		    AccessibilityNodeInfo newNodeInfo = AccessibilityHelper.findNodeInfosByText(lastNodeInfo, WechatConfig.ENVELOPE_TEXT_KEY_OTHER);
		    if (null == newNodeInfo) {
		        newNodeInfo = AccessibilityHelper.findNodeInfosByText(lastNodeInfo, WechatConfig.ENVELOPE_TEXT_KEY);
		    }
		    Log.d(TAG, "handleChatListRedEnvelope newNodeInfo : " + newNodeInfo);
		        
		    Log.i(TAG, "handleChatListRedEnvelope isFinishRobEnvelope : " + isFinishRobEnvelope);
		    if (null != newNodeInfo) {
				isFinishRobEnvelope = false;
				isNeedToDeal = true;
				clickNewNodeInfo(newNodeInfo);
		    } 
        }
        Log.d(TAG, "handleChatListRedEnvelope isFinishRobEnvelope : " + isFinishRobEnvelope);
        Log.d(TAG, "handleChatListRedEnvelope end");
	}
	
	private boolean isRealRedPacket(String des) {
		if (null == des || des.isEmpty()) {
			Log.w(TAG, "isRedPacket descriptionStr is null or isEmpty return");
			return false;
		}
		String []desArray = des.split(",");  //name,time,[微信红包],description
		if (desArray.length <4) {
			Log.w(TAG, "isRedPacket is fake red packet return");
			return false;
		}
		if (desArray[3].isEmpty()) {
			Log.i(TAG, "isRedPacket description isEmptyreturn");
		}
		return true;
	}
	
	private String getChatListTitle(AccessibilityNodeInfo mNodeInfo) {
		String listTitle = "";
		int wechatVersion = getWechatVersion();
		AccessibilityNodeInfo listTitleNodeInfo = null;
		String listTitleId = WechatConfig.getChatUiTitleId(wechatVersion); //actionbar名称的text id

		if (listTitleId != null) {
			listTitleNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, listTitleId);
		}
		if (null != listTitleNodeInfo) {
			listTitle = String.valueOf(listTitleNodeInfo.getText());
			Log.i(TAG, "getChatListTitle id listTitle : " + listTitle);
		}

		if (null == listTitle || listTitle.isEmpty() || "null".equals(listTitle)) {
	        List<AccessibilityNodeInfo> list = mNodeInfo.findAccessibilityNodeInfosByText(WechatConfig.RETURN_KEY);

	        if(list != null && !list.isEmpty()) {
	            AccessibilityNodeInfo parent = null;
	            for(AccessibilityNodeInfo node : list) {
	                if(!WechatConfig.IMAGEVIEW_CLASS_NAME.equals(node.getClassName())) {
	                    continue;
	                }
	                String desc = String.valueOf(node.getContentDescription());
	                if(!WechatConfig.RETURN_KEY.equals(desc)) {
	                    continue;
	                }
	                parent = node.getParent();
	                break;
	            }
	            if(parent != null) {
	                parent = parent.getParent();
	            }
	            if(parent != null) {
					for (int i=0;i<parent.getChildCount();i++){
						AccessibilityNodeInfo node = parent.getChild(i);
						Log.d(TAG, "getChatListTitle  node.classname  :"+i+" --" +node.getClassName());
						if(WechatConfig.TEXTVIEW_CLASS_NAME.equals(node.getClassName())) {
							listTitle = String.valueOf(node.getText());
							Log.d(TAG, "getChatListTitle  title : " +listTitle);
							break;
						}
					}
	            }
	        }

			if (null == listTitle || listTitle.isEmpty() || "null".equals(listTitle)) {
				listTitle = getChatListTitleTo(mNodeInfo);
			}
        }
		Log.d(TAG, "getChatListTitle listTitle : " + listTitle);
		return listTitle;
	}
	
	private String getMessageFromNode(AccessibilityNodeInfo node) {
		String text = "null";
        if (node == null)
            return text;
       
        AccessibilityNodeInfo messageNode = node;
        Rect bounds = new Rect();
        messageNode.getBoundsInScreen(bounds);
        if (bounds.top < 0)
        	return text;
        int count = messageNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo thisNode = messageNode.getChild(i);
			if (thisNode == null)
				continue;
            if (WechatConfig.TEXTVIEW_CLASS_NAME.equals(thisNode.getClassName())) {
            	text = String.valueOf(thisNode.getText());
            }
            /*//获取微信名称
            if (WechatConfig.IMAGEVIEW_CLASS_NAME.equals(thisNode.getClassName())) {
                CharSequence contentDescription = thisNode.getContentDescription();
                CharSequence result = contentDescription.toString().replaceAll("头像$", "");
            }*/
        }
        return text;
    }
	
	/** 是否为群聊天*/
    private boolean isMemberChatUi(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
			Log.d(TAG, "isMemberChatUi nodeinfo is null");
            return false;
        }
        
        int wechatVersion = getWechatVersion();
        String id = WechatConfig.getChatUiTitleId(wechatVersion);
        
        String title = null;
		AccessibilityNodeInfo target = null;
		if (id != null) {
			target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
		}
        if(target != null) {
            title = String.valueOf(target.getText());
        }

		if (null == title || title.isEmpty() || "null".equals(title)) {
	        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(WechatConfig.RETURN_KEY);
			Log.d(TAG, "(WechatAccessiblity.java:1014)  list" +list);

	        if(list != null && !list.isEmpty()) {
	            AccessibilityNodeInfo parent = null;
	            for(AccessibilityNodeInfo node : list) {
	                if(!WechatConfig.IMAGEVIEW_CLASS_NAME.equals(node.getClassName())) {
	                    continue;
	                }
					Log.d(TAG, "(WechatAccessiblity.java:1022)  list" +node);
					String desc = String.valueOf(node.getContentDescription());
	                if(!WechatConfig.RETURN_KEY.equals(desc)) {
	                    continue;
	                }
	                parent = node.getParent();
	                break;
	            }
	            if(parent != null) {
	                parent = parent.getParent();
	            }
				Log.d(TAG, "(WechatAccessiblity.java:1022)  parent" +parent.getChildCount() +"  --parent" +parent);
				if(parent != null) {
	                if( parent.getChildCount() >= 2) {
						for (int i=0;i<parent.getChildCount();i++){
							AccessibilityNodeInfo node = parent.getChild(i);
							Log.d(TAG, "(WechatAccessiblity.java:1039)  node.classname  :"+i+" --" +node.getClassName());
							if(WechatConfig.TEXTVIEW_CLASS_NAME.equals(node.getClassName())) {
								title = String.valueOf(node.getText());
								Log.d(TAG, "(WechatAccessiblity.java:1042)  title" +title);
								break;
							}
						}
	                }
	            }
	        }

			if (null == title || title.isEmpty() || "null".equals(title)) {
				title = getChatListTitleTo(nodeInfo);
			}
        }

		Log.d(TAG, "isMemberChatUi title : " + title);
        if(title != null && title.endsWith(")")) {
            return true;
        }
        return false;
    }
    
    /** 获取微信的版本*/
    private int getWechatVersion() {
    	PackageInfo mWechatPackageInfo = null;
    	try {
            mWechatPackageInfo = mContext.getPackageManager().getPackageInfo(WechatConfig.WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(mWechatPackageInfo == null) {
            return 0;
        }
        Log.i(TAG, "versionCode : " + mWechatPackageInfo.versionCode);
        return mWechatPackageInfo.versionCode;
    }
    
    public void clearNodeInfoList() {
    	mNewNodeInfoList.clear();
    	mPendingIntentList.clear();
    }
    
    private void updateRecordItemCount(String keyValue, int count) {
		Log.d(TAG, "updateRecordItemCount keyValue : " + keyValue + " count : " + count);
    	recordItemCount.put(keyValue, count);
    }
    
    private int getRecordItemCount(String keyValue) {
    	int count = 0;
    	if (recordItemCount.containsKey(keyValue)) {
    		Object temp = recordItemCount.get(keyValue);
    		count = Integer.parseInt(String.valueOf(temp));
    	}
    	
    	return count;
    }
    
    private boolean isNewLastRedEnvelopeNode(AccessibilityNodeInfo nodeInfo) {
    	AccessibilityNodeInfo envelopeNodeInfo = AccessibilityHelper.findNodeInfosByText(nodeInfo, WechatConfig.ENVELOPE_TEXT_KEY_OTHER);
        if (null == envelopeNodeInfo) {
        	envelopeNodeInfo = AccessibilityHelper.findNodeInfosByText(nodeInfo, WechatConfig.ENVELOPE_TEXT_KEY);
        }
        if (null == envelopeNodeInfo) {
        	return false;
        }
        
        AccessibilityNodeInfo mNodeInfo = envelopeNodeInfo.getParent();
		if (null == mNodeInfo) {
			return false;
		}
		mNodeInfo = mNodeInfo.getParent();
		if (null == mNodeInfo) {
			return false;
		}
		
		String nodeTime = getMessageFromNode(envelopeNodeInfo);
		String currentTime = getDateTimeToStr(System.currentTimeMillis());
		if (null == nodeTime || null == currentTime) {
			return false;
		}
		
		String week = mContext.getResources().getString(R.string.week);
		String yesterday = mContext.getResources().getString(R.string.yesterday);
		if (nodeTime.contains(week) || nodeTime.contains(yesterday)) {
			return false;
		}
		
		try {
			String [] nodeTimeArrary = nodeTime.split(":");
			String [] currentTimeArrary = nodeTime.split("");
			String [] curTimeArrary = currentTimeArrary[1].split(":");
			int nodeHour = Integer.parseInt(nodeTimeArrary[0]);
			int nodeMinute = Integer.parseInt(nodeTimeArrary[1]);
			int curHour = Integer.parseInt(curTimeArrary[0]);
			int curMinute = Integer.parseInt(curTimeArrary[1]);
			if (nodeHour > curHour) {
				return false;
			} else if (nodeMinute <= curMinute + 1) {
				return true;
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
    	return false;
    }
    
    private AccessibilityNodeInfo getMsgEditTextNodeInfo(AccessibilityNodeInfo mNodeInfo) {
    	AccessibilityNodeInfo editTextNode = null; 
    	editTextNode = recycleFindEditText(mNodeInfo);
    	return editTextNode;
    }
    
    private AccessibilityNodeInfo recycleFindEditText(AccessibilityNodeInfo info) {    
        if (info.getChildCount() == 0) { 
        	if (WechatConfig.EDITTEXT_CLASS_NAME.equals(info.getClassName())) {
        		return info;
			}
        } else {    
            for (int i = 0; i < info.getChildCount(); i++) {    
            	AccessibilityNodeInfo tmp = recycleFindEditText(info.getChild(i));
            	if (tmp != null) 
            		return tmp;
            }    
        } 
        return null;
    }

	private String getChatListTitleTo(AccessibilityNodeInfo mNodeInfo) {
		String listTitle = "";
		if(mNodeInfo == null) {
			Log.d(TAG, "getChatListTitleTo mNodeInfo is null");
			return listTitle;
		}

		int wechatVersion = getWechatVersion();
		AccessibilityNodeInfo listTitleNodeInfo = null;
		String listTitleId = WechatConfig.getChatUiTitleId(wechatVersion); //actionbar名称的text id
		if (listTitleId != null) {
			listTitleNodeInfo = AccessibilityHelper.findNodeInfosById(mNodeInfo, listTitleId);
		}
		if (null != listTitleNodeInfo) {
			listTitle = String.valueOf(listTitleNodeInfo.getText());
			Log.i(TAG, "getChatListTitleTo id listTitle : " + listTitle);
		}

		if (null == listTitle || listTitle.isEmpty() || "null".equals(listTitle)) {
			List<AccessibilityNodeInfo> list = mNodeInfo.findAccessibilityNodeInfosByText(WechatConfig.WETCHAT_KEY);
			Log.i(TAG, "(WechatAccessiblity.java:1168)  list" + list);

			if(list != null && !list.isEmpty()) {
				AccessibilityNodeInfo parent = null;
				for(AccessibilityNodeInfo node : list) {
					if(!WechatConfig.IMAGEVIEW_CLASS_NAME.equals(node.getClassName())) {
						continue;
					}
					String desc = String.valueOf(node.getContentDescription());
					if(!WechatConfig.WETCHAT_KEY.equals(desc)) {
						continue;
					}
					parent = node.getParent();
					break;
				}
				if(parent != null) {
					parent = parent.getParent();
				}
				if(parent != null) {
					if( parent.getChildCount() >= 2) {
						for (int i=0;i<parent.getChildCount();i++){
							AccessibilityNodeInfo node = parent.getChild(i);
							if(WechatConfig.TEXTVIEW_CLASS_NAME.equals(node.getClassName())) {
								listTitle = String.valueOf(node.getText());
								break;
							}
						}
					}
				}
			}
		}
		Log.d(TAG, "getChatListTitleTo listTitle : " + listTitle);
		return listTitle;
	}

	//start by huanggq for mm video
	private static final int VIDEO_WINDOW_NONE = 0;
	private static final int VIDEO_WINDOW_TIME_LINE_UI = 1;
	private static final int VIDEO_WINDOW_PLAY_UI = 2;
	private static final int VIDEO_WINDOW_LAUNCHER_UI= 3;
	private static final int VIDEO_WINDOW_UPLOAD_UI = 4;
	private static final int VIDEO_WINDOW_GALLERY_UI = 5;

	private int mCurrentWindow_video = VIDEO_WINDOW_NONE;

	MMVideoManager mmVideoManager;
	private int mm_version = 821;


	private static final int CHECk_POP_VISIABLE = 1000;
	private static final int CHECK_PLAY_STATE = CHECk_POP_VISIABLE + 1;
	private static final int TIME = 10;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case CHECk_POP_VISIABLE:
					check();
					break;
				case CHECK_PLAY_STATE:
					checkPlayState();
					break;
				default:
					break;

			}
		}
	};

	private void checkPlayState() {
		if (mService != null) {

			AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();

			if(rootInActiveWindow != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					rootInActiveWindow.refresh();
				}

				String res_play = WechatConfig.PLAY_UI_ID;
				if (mm_version == 821) {
					res_play = WechatConfig.PLAY_UI_ID_821;
				} else if (mm_version == 840) {
					res_play = WechatConfig.PLAY_UI_ID_840;
				} else if (mm_version == 861 || mm_version == 860) {
					res_play = WechatConfig.PLAY_UI_ID_861;
				} else if (mm_version == 880) {
					res_play = WechatConfig.PLAY_UI_ID_880;
				}
				Log.i(TAG, "checkPlayState res_play " + res_play);
				AccessibilityNodeInfo	pop = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
						res_play);

				if(pop != null) {
					pop.recycle();
					Log.i(TAG, "check play fail");
					mHandler.removeMessages(CHECK_PLAY_STATE);
					mHandler.sendEmptyMessageDelayed(CHECK_PLAY_STATE, TIME);
				} else {
					Log.i(TAG, "check play success ");
					mmVideoManager.dostopFloat();
					mHandler.removeMessages(CHECK_PLAY_STATE);
				}
				rootInActiveWindow.recycle();

			} else {

				mmVideoManager.dostopFloat();
				mHandler.removeMessages(CHECK_PLAY_STATE);
			}
		}

	}

	private void check() {
Log.d("warlock","check  --->");
		//start by guojun topwise fix bug#9650
		//if (mService != null) {
		if (mService != null && WechatConfig.WECHAT_TIMELINE_UI.equals(mCurrentClassName)) {
		//end by guojun topwise fix bug#9650

			AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();

			if(rootInActiveWindow != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					rootInActiveWindow.refresh();
				}

				String resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT;
				String resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID;
				if (mm_version == 821) {
					resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_821;
				} else if (mm_version == 840) {
					resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_840;
					resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID_840;
				} else if (mm_version == 860 || mm_version == 861) {
					resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_861;
					resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID_861;
				} else if (mm_version == 880) {
					resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_880;
					resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID_880;
				}
				Log.i(TAG, "checkPlayState check " + resId_click);
				AccessibilityNodeInfo	pop = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
						resId_click);
				//start by guojun fix bug#8804 and bug#8807  
			//	AccessibilityNodeInfo shortVideo = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
			//			WechatConfig.WECHAT_SHORT_VIDEO_UI_ID);
				List<AccessibilityNodeInfo> shortVideoList =
						null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
					shortVideoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId_video);
				}
				if(pop != null && shortVideoList!= null) {
					Rect poprect = new Rect();
					Rect shortvideorect = new Rect();
					pop.getBoundsInScreen(poprect);
					pop.recycle();
					boolean isLastShotVideo = false;
					for (AccessibilityNodeInfo shortVideo:shortVideoList){
						shortVideo.getBoundsInScreen(shortvideorect);
						shortVideo.recycle();
						if (poprect.top - shortvideorect.bottom ==3)
							isLastShotVideo =true;
					}
					Log.i(TAG, "check fail");
					if (isLastShotVideo){
						mHandler.removeMessages(CHECk_POP_VISIABLE);
						mHandler.sendEmptyMessageDelayed(CHECk_POP_VISIABLE, TIME);
					}else {
						Log.i(TAG, "check success ");
						mmVideoManager.dostop();
						mHandler.removeMessages(CHECk_POP_VISIABLE);
					}
					isLastShotVideo = false;
				//end by guojun fix bug#8804 and bug#8807
				} else {
					Log.i(TAG, "check success ");
					mmVideoManager.dostop();
					mHandler.removeMessages(CHECk_POP_VISIABLE);
				}
				rootInActiveWindow.recycle();
			}
		}
	}


	private void showTimeFloat() {
		Log.d("warlock"," show TimeFloat -->");
		if ( false) {
			return;
		}
		AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();

		if (rootInActiveWindow != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				rootInActiveWindow.refresh();
			}
			String resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT;
			String resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID;
			if (mm_version == 821) {
				resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_821;
			} else if (mm_version == 840) {
				resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_840;
				resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID_840;
			} else if (mm_version == 860 || mm_version == 861) {
				resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_861;
				resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID_861;
			} else if (mm_version == 880) {
				resId_click = WechatConfig.TIMELINE_COMMENT_EIGHT_880;
				resId_video = WechatConfig.WECHAT_SHORT_VIDEO_UI_ID_880;
			}
			AccessibilityNodeInfo pop = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
					resId_click);
			//start by guojun fix bug#8804 and bug#8807  
			AccessibilityNodeInfo shortVideo = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
					resId_video);
			//end by guojun fix bug#8804 and bug#8807

			//find comment rect .use it to place floatbar and click one or two
			if (pop != null && shortVideo!=null) {
				Rect rect = new Rect();
				pop.getBoundsInScreen(rect);
				pop.recycle();
				Log.i(TAG, "TIMELINE_COMMENT_PLAYING rect " + rect);
				//start by guojun fix bug#8804 and bug#8807  
				Rect videoRect = new Rect();
				shortVideo.getBoundsInScreen(videoRect);
				shortVideo.recycle();
				//end by guojun fix bug#8804 and bug#8807  

				int clickTime = 1;
				String resplay = WechatConfig.TIMELINE_COMMENT_PLAYING;
				if (mm_version == 821) {
					resplay = WechatConfig.TIMELINE_COMMENT_PLAYING_821;
				} else if (mm_version == 840) {
					resplay = WechatConfig.TIMELINE_COMMENT_PLAYING_840;
				} else if (mm_version == 861 || mm_version == 860) {
					resplay = WechatConfig.TIMELINE_COMMENT_PLAYING_861;
				} else if (mm_version == 880) {
					resplay = WechatConfig.TIMELINE_COMMENT_PLAYING_880;
				}
				List<AccessibilityNodeInfo> list = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
					list = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resplay);
				}

				Log.i(TAG, "list size " + list.size());

				for (int i = list.size() - 1; i >= 0; i--) {

					Rect nodeInforect = new Rect();
					AccessibilityNodeInfo nodeInfo = list.get(i);
					if (nodeInfo != null) {

						nodeInfo.getBoundsInScreen(nodeInforect);
						nodeInfo.recycle();
						Log.i("huanggq", "nodeInforect " + nodeInforect);
						if (rect.top > nodeInforect.top) {
							if (rect.top - nodeInforect.top < 500) {
								clickTime = 2;
							}
							break;
						}
					}

				}
				Log.i(TAG, "clickTime: " + clickTime);

				List<AccessibilityNodeInfo> shortVideoList =
						null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
					shortVideoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId_video);
				}
				boolean isLastShotVideo =false;
				for (AccessibilityNodeInfo shortmmVideo:shortVideoList){
					shortmmVideo.getBoundsInScreen(videoRect);
					shortmmVideo.recycle();
					if (rect.top - videoRect.bottom ==3)
						isLastShotVideo =true;
				}
				//start by guojun fix bug#8804 and bug#8807  
				if (isLastShotVideo){
					mmVideoManager.dostart(rect.left, rect.top, clickTime);
					isLastShotVideo =false;
				}
				//end by guojun fix bug#8804 and bug#8807  
				mHandler.removeMessages(CHECk_POP_VISIABLE);
				mHandler.sendEmptyMessageDelayed(CHECk_POP_VISIABLE, TIME);
			}
			rootInActiveWindow.recycle();
		}

	}
	/**
	 *
	 * 处理点击事件
	 *
	 */

	private void handleClickEvent(AccessibilityEvent event) {
		Log.i(TAG, "handleClickEvent");

		AccessibilityNodeInfo source = event.getSource();
		if (source == null) {
			return;
		}

		if (mContext.getString(R.string.comment_six_6_des).equals(source.getContentDescription())) {
			showTimeFloat();
		}

	}

	private void handleWindowStateChange(AccessibilityEvent event) {

		mm_version = getWechatVersion();

		String className = String.valueOf(event.getClassName());
		mCurrentClassName = className;
		Log.i(TAG, "handleWindowStateChange " + className);

		if (WechatConfig.WECHAT_TIMELINE_UI.equals(className)) {

			handleTimeLineUI(event);

		} else if (WechatConfig.WECHAT_PLAY_UI.equals(className)) {

			handlePlayUI(event);

		} else if (WechatConfig.WECHAT_UPLOAD_UP.equals(className)) {

			handleUploadUI(event);

		} else if (WechatConfig.WECHAT_LAUNCHER_UI.equals(className)) {

			handleLauncherUI(event);

		} else if (WechatConfig.WECHAT_GALLERY_UI.equals(className)) {

			handleGalleryUI(event);
		}
		check();
	}

	private void handleGalleryUI(AccessibilityEvent event) {

		Log.i(TAG, "handleGalleryUI");
		mCurrentWindow_video = VIDEO_WINDOW_GALLERY_UI;
		AccessibilityNodeInfo source = event.getSource();
		if (source == null) {
			return;
		}

		if (!checkGalleryState()) {
			return;
		}

		showPlayFloat(event);

	}

	private boolean checkGalleryState() {


		if (mService != null) {

			AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();

			if(rootInActiveWindow != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					rootInActiveWindow.refresh();
				}

				String res_play = WechatConfig.GALLERY_UI_VIDEO;
				if (mm_version == 840) {
					res_play = WechatConfig.GALLERY_UI_VIDEO_840;
				} else if (mm_version == 861 || mm_version == 860) {
					res_play = WechatConfig.GALLERY_UI_VIDEO_861;
				} else if (mm_version == 880) {
					res_play = WechatConfig.GALLERY_UI_VIDEO_880;
				}
				AccessibilityNodeInfo	pop = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
						res_play);

				if(pop != null) {
					pop.recycle();
					String res_play_editor = WechatConfig.GALLERY_UI_VIDEO_EDITOR;
					if (mm_version == 840) {
						res_play_editor = WechatConfig.GALLERY_UI_VIDEO_EDITOR_840;
					} else if (mm_version == 860 || mm_version == 861) {
						res_play_editor = WechatConfig.GALLERY_UI_VIDEO_EDITOR_861;
					} else if (mm_version == 880) {
						res_play_editor = WechatConfig.GALLERY_UI_VIDEO_EDITOR_880;
					}
					AccessibilityNodeInfo	editor = AccessibilityHelper.findNodeInfosById(rootInActiveWindow,
							res_play_editor);

					if (editor != null) {
						editor.recycle();
						rootInActiveWindow.recycle();
						return  false;
					}
					rootInActiveWindow.recycle();
					return true;

				}
				rootInActiveWindow.recycle();

			}
		}

		return false;

	}



	private void handlePlayUI(AccessibilityEvent event) {
		Log.i(TAG, "handlePlayUI");
		mCurrentWindow_video = VIDEO_WINDOW_PLAY_UI;
		showPlayFloat(event);
	}


	private void showPlayFloat(AccessibilityEvent event) {

		AccessibilityNodeInfo source = event.getSource();
		if (source == null) {
			return;
		}
		source.recycle();
		mmVideoManager.dostartPlayFloat();

		mHandler.removeMessages(CHECK_PLAY_STATE);
		mHandler.sendEmptyMessageDelayed(CHECK_PLAY_STATE, TIME);

	}


	private void handleUploadUI(AccessibilityEvent event) {
		Log.i(TAG, "handleUploadUI");

		if(mCurrentWindow_video == VIDEO_WINDOW_PLAY_UI) {

			mmVideoManager.dostopFloat();
		}

		mCurrentWindow_video = VIDEO_WINDOW_UPLOAD_UI;
	}

	private void handleLauncherUI(AccessibilityEvent event) {
		Log.i(TAG, "handleLauncherUI");

		if (mCurrentWindow_video == VIDEO_WINDOW_GALLERY_UI) {
			mHandler.removeMessages(CHECK_PLAY_STATE);
			mmVideoManager.dostopFloat();
		}

		mCurrentWindow_video = VIDEO_WINDOW_LAUNCHER_UI;
	}


	private void handleTimeLineUI(AccessibilityEvent event) {
		Log.i(TAG, "handleTimeLineUI ");

		if (mCurrentWindow_video == VIDEO_WINDOW_PLAY_UI ||
				mCurrentWindow_video == VIDEO_WINDOW_TIME_LINE_UI ||
				mCurrentWindow_video == VIDEO_WINDOW_UPLOAD_UI) {
			mmVideoManager.dostopFloat();

			showTimeFloat();
		}


		mCurrentWindow_video = VIDEO_WINDOW_TIME_LINE_UI;

	}
	//end by huanggq   for mm vide

	private int getRandomDelayTime() {
		int randomTime = 0;
		//(int)( Math.random() * ( b – a)) + a;
		randomTime = (int)(Math.random()*(Config.RANDOM_DELAY_TIME_END - Config.RANDOM_DELAY_TIME_START))
			+ Config.RANDOM_DELAY_TIME_START;
		Log.d(TAG, "getRandomDelayTime randomTime : " + randomTime);
		return randomTime;
	}

	//start, add by zl topwise for bug[10219]
	public void isTooSlowOpen() {
		Log.d(TAG, "isTooSlowOpen");
		AccessibilityNodeInfo mNodeInfo = mService.getRootInActiveWindow();
		if (null == mNodeInfo) {
			isFinishRobEnvelope = true;
			isNotificationRedEnvelope = false;
			isAutoReturn = false;
			Log.d(TAG, "isTooSlowOpen mNodeInfo is null");
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mNodeInfo.refresh();
		}
		AccessibilityNodeInfo targetNode = null;
		int wechatVersion = getWechatVersion();
		if (wechatVersion < WechatConfig.USE_ID_MIN_VERSION) {
			targetNode = AccessibilityHelper.findNodeInfosByText(mNodeInfo, WechatConfig.OPEN_ENVELOPE_KEY);
		} else {
			String buttonId = WechatConfig.getOpenButtonId(wechatVersion);

			if(buttonId != null) {
				targetNode = AccessibilityHelper.findNodeInfosById(mNodeInfo, buttonId);
			}

			if(targetNode == null) {
				//分别对应固定金额的红包 拼手气红包
				AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(mNodeInfo,
						WechatConfig.MONEY_RECEIVE_TEXT1, WechatConfig.MONEY_RECEIVE_TEXT2, WechatConfig.MONEY_RECEIVE_TEXT3);

				if(textNode != null) {
					for (int i = 0; i < textNode.getChildCount(); i++) {
						AccessibilityNodeInfo node = textNode.getChild(i);
						if (WechatConfig.BUTTON_CLASS_NAME.equals(node.getClassName())) {
							targetNode = node;
							break;
						}
					}
				}
			}

			if(targetNode == null) { //通过组件查找
				targetNode = AccessibilityHelper.findNodeInfosByClassName(mNodeInfo, WechatConfig.BUTTON_CLASS_NAME);
			}
		}

		Log.d(TAG, "isTooSlowOpen targetNode : " + targetNode);
		Log.d(TAG, "isTooSlowOpen isAutoReturn : " + isAutoReturn + " isNotificationRedEnvelope : "
				+ isNotificationRedEnvelope);
		if (null == targetNode) {
			if (isAutoReturn && isNotificationRedEnvelope) {
				AccessibilityHelper.performBack(mService);
			} else if (isAutoReturn) {
				AccessibilityHelper.performBack(mService);  //return
			}
			if (isNotificationRedEnvelope) {
				isCheckTooSlow = true;
			}
			isFinishRobEnvelope = true;
			isNotificationRedEnvelope = false;
//			isAutoReturn = false;
		} else {
			targetNode = AccessibilityHelper.findNodeInfosByTexts(mNodeInfo, WechatConfig.WETCHAT_TOO_SLOW_KEY,
					WechatConfig.WETCHAT_TOO_SLOW_KEY1, WechatConfig.WETCHAT_TOO_SLOW_KEY2,
					WechatConfig.WETCHAT_TOO_SLOW_KEY3, WechatConfig.WETCHAT_TOO_SLOW_KEY3);
			if (null != targetNode) {
				if (isAutoReturn && isNotificationRedEnvelope) {
					AccessibilityHelper.performBack(mService);
				} else if (isAutoReturn) {
					AccessibilityHelper.performBack(mService);  //return
				}
				if (isNotificationRedEnvelope) {
					isCheckTooSlow = true;
				}
				isFinishRobEnvelope = true;
				isNotificationRedEnvelope = false;
				isAutoReturn = false;
			} else {
				mRedHandle.postDelayed(mCheckIsTooSlow, Config.CHECK_IS_TOO_SLOW_DELAY_TIME);
			}
		}
	}

	public Runnable mCheckIsTooSlow = new Runnable() {
		@Override
		public void run() {
			isTooSlowOpen();
		}
	};

	private void resetRecordItemCount(AccessibilityEvent event) {
		if (!isCheckTooSlow) {
			return;
		}
		AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
		if(nodeInfo == null) {
			Log.w(TAG, "resetRecordItemCount nodeInfo is null");
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			nodeInfo.refresh();
		}
		String titleName = getChatListTitle(nodeInfo);
		if (titleName.isEmpty() || "null".equals(titleName)) { //非聊天界面
			Log.i(TAG, "resetRecordItemCount titleName.isEmpty() return");
			return;
		}

		int itemCount = getRecordItemCount(titleName);
		Log.i(TAG, "resetRecordItemCount itemCount : " + itemCount);
		if (itemCount > 0)
			updateRecordItemCount(titleName, itemCount - 1);
		isCheckTooSlow = false;
	}
	//end, add by zl topwise for bug[10219]
}
