package com.android.redenvelope;

public class QQConfig {
	public static final String QQ_NOTIFY_TEXT = "[QQ红包]";
	public static final String QQ_RED_PACKET_TEXT = "QQ红包";
	public static final String CLICK_OPEN_TEXT = "点击拆开";
	public static final String HAS_OPEN_TEXT = "已拆开";
	public static final String CHECK_DETAIL_TEXT = "查看详情";
	public static final String CHECK_GET_DETAIL_TEXT = "查看领取详情";
	public static final String PASSWORD_RED_PACKET_TEXT = "口令红包";
	public static final String PASSWORD_TEXT = "口令:";
	public static final String MESSAGE_KEY = "消息";
	public static final String ONLINE_TEXT = "在线";
	public static final String SEND_BUTTON_TEXT = "发送";
	
	public static final String QQ_LAUNCH_UI = "com.tencent.mobileqq.activity.SplashActivity";
	public static final String QQ_WALLETPLUGIN_UI = "cooperation.qwallet.plugin.QWalletPluginProxyActivity";
	public static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
	public static final String QQ_RECORD_UI = "cooperation.qwallet.plugin.QWalletPluginProxyActivity";
	public static final String TEXTVIEW_CLASS_NAME = "android.widget.TextView";
	
	/**详情界面显示抢到的钱的text的ID**/
	public static final String MONEY_TEXT_ID = "com.tencent.mobileqq:id/hb_count_tv";
	
	/**详情界面显示谁的红包的text的ID**/
	public static final String FROM_NAME_TEXT_ID = "com.tencent.mobileqq:id/sender_info";
	
	/**QQ聊天界面发送信息框ID**/
	private static final String EDITTEXT_ID = "com.tencent.mobileqq:id/input";
	
	/**QQ聊天界面发送信息的按钮ID**/
	private static final String SEND_BUTTON_ID = "com.tencent.mobileqq:id/fun_btn";
	
	/**QQ聊天界面返回按钮ID**/
	private static final String RETURN_BUTTON_ID = "com.tencent.mobileqq:id/ivtitleBtnLeft";
	
	/**QQ聊天界面标题text ID**/
	private static final String CHAT_LIST_TITLE_ID = "com.tencent.mobileqq:id/title";
	
	/**QQ聊天界面子标题text ID**/
	private static final String CHAT_LIST_SUB_TITLE_ID = "com.tencent.mobileqq:id/title_sub";
	
	
	/**
	 * 6.3.1 code 350
	 */
	
	
	
	/**QQ红包详情界面显示谁发的红包的text ID**/
	public static String getFromNameTextId(int versionCode) {
		String fromNameTextId = null;
    	switch (versionCode) {
		case 350:
			fromNameTextId = FROM_NAME_TEXT_ID;
			break;
		default:
			fromNameTextId = FROM_NAME_TEXT_ID;
			break;
    	}
    	return fromNameTextId;
	}
	
	 /**QQ红包详情界面显示多少元的text ID**/
    public static String getMoneyTextId(int versionCode) {
    	String moneyTextId = null;
    	switch (versionCode) {
		case 350:
			moneyTextId = MONEY_TEXT_ID;
			break;
		default:
			moneyTextId = MONEY_TEXT_ID;
			break;
		}
    	return moneyTextId;
    }
    
    /**QQ输入信息框ID**/
    public static String getQQMobileEditTextId(int versionCode) {
    	String editEextId = null;
    	switch (versionCode) {
	    case 350:
	    	editEextId = EDITTEXT_ID;
	    	break;
	    default:
	    	editEextId = EDITTEXT_ID;
	    	break;
    	}
    	return editEextId;
    }
    
    /**QQ发送信息按钮ID**/
    public static String getSendButtonId(int versionCode) {
    	String sendButtonId = null;
    	switch (versionCode) {
    	case 350:
    		sendButtonId = SEND_BUTTON_ID;
    		break;
    	default:
    		sendButtonId = SEND_BUTTON_ID;
    		break;
    	}
    	return sendButtonId;
    }
    
    /**QQ聊天界面返回按钮ID**/
    public static String getChatUiReturnBtnId(int versionCode) {
    	String returnButtonId = null;
    	switch (versionCode) {
    	case 350:
    		returnButtonId = RETURN_BUTTON_ID;
    		break;
    	default:
    		returnButtonId = RETURN_BUTTON_ID;
    		break;
    	}
    	return returnButtonId;
    }
    
    /**QQ聊天界面标题text ID**/
	public static String getChatListTitleId(int versionCode) {
		String titleId = null;
    	switch (versionCode) {
    	case 350:
    		titleId = CHAT_LIST_TITLE_ID;
    		break;
    	default:
    		titleId = CHAT_LIST_TITLE_ID;
    		break;
    	}
    	return titleId;
	}
	
	/**QQ聊天界面子标题text ID**/
	public static String getChatListSubTitleId(int versionCode) {
		String subTitleId = null;
    	switch (versionCode) {
    	case 350:
    		subTitleId = CHAT_LIST_SUB_TITLE_ID;
    		break;
    	default:
    		subTitleId = CHAT_LIST_SUB_TITLE_ID;
    		break;
    	}
    	return subTitleId;
	}
}
