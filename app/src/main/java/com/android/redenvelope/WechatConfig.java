package com.android.redenvelope;

public class WechatConfig {
	public static final String ENVELOPE_TEXT_KEY = "[微信红包]";
	public static final String ENVELOPE_TEXT_KEY_OTHER = "领取红包";
	public static final String SEND_KEY = "发送";
	public static final String WHO_ENVELOPE_KEY = "的红包";
	public static final String OPEN_ENVELOPE_KEY = "拆红包";
	public static final String RETURN_KEY = "返回";
	public static final String MONEY_RECEIVE_TEXT1 = "发了一个红包";
	public static final String MONEY_RECEIVE_TEXT2 = "给你发了一个红包";
	public static final String MONEY_RECEIVE_TEXT3 = "发了一个红包，金额随机";
	public static final String HAS_OPEN_MSG_TEXT = "领取了";
	public static final String MONEY_TEXT_KEY = ".";
	public static final String WETCHAT_KEY = "微信";
	public static final String WETCHAT_TOO_SLOW_KEY = "看看大家的手气";
	public static final String WETCHAT_TOO_SLOW_KEY1 = "手慢了，红包派完了";
	public static final String WETCHAT_TOO_SLOW_KEY2 = "手慢了";
	public static final String WETCHAT_TOO_SLOW_KEY3 = "红包派完了";
	public static final String WETCHAT_TOO_SLOW_KEY4 = "派完了";

	public static final String WECHAT_RECEIVE_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
	public static final String WECHAT_LAUNCHER_UI = "com.tencent.mm.ui.LauncherUI";
	public static final String WECHAT_CHATTING_UI = "com.tencent.mm.ui.chatting.ChattingUI";
	public static final String WECHAT_DETAIL_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
	public static final String WECHAT_RECORD_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyMyRecordUI";
	public static final String WECHAT_PACKAGENAME = "com.tencent.mm";
	public static final	String WECHAT_WALLETPAY_UI = "com.tencent.mm.plugin.wallet.pay.ui.WalletPayUI";

	public static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700
	public static final String BUTTON_CLASS_NAME = "android.widget.Button";
	public static final String TEXTVIEW_CLASS_NAME = "android.widget.TextView";
	public static final String IMAGEVIEW_CLASS_NAME = "android.widget.ImageView";
	public static final String EDITTEXT_CLASS_NAME = "android.widget.EditText";
	public static final String LISTVIEW_CLASS_NAME = "android.widget.ListView";

	/**微信聊天界面发送信息框ID**/
	private static final String EDITTEXT_ID_760 = "com.tencent.mm:id/c50";
	private static final String EDITTEXT_ID_780 = "com.tencent.mm:id/c6v";
	private static final String EDITTEXT_ID_800 = "com.tencent.mm:id/yq";
	private static final String EDITTEXT_ID_821 = "com.tencent.mm:id/yv";
	private static final String EDITTEXT_ID_840 = "com.tencent.mm:id/z4";
	private static final String EDITTEXT_ID_861 = "com.tencent.mm:id/z4";//861的这个id和840是一样的
	private static final String EDITTEXT_ID_880 = "com.tencent.mm:id/a1o";

	/**微信聊天界面发送信息的按钮ID**/
	private static final String SEND_BUTTON_ID_760 = "com.tencent.mm:id/c55";
	private static final String SEND_BUTTON_ID_780 = "com.tencent.mm:id/c70";
	private static final String SEND_BUTTON_ID_800 = "com.tencent.mm:id/yw";
	private static final String SEND_BUTTON_ID_821 = "com.tencent.mm:id/z1";
	private static final String SEND_BUTTON_ID_840 = "com.tencent.mm:id/z_";
	private static final String SEND_BUTTON_ID_861 = "com.tencent.mm:id/z_";//861的这个id和840是一样的
	private static final String SEND_BUTTON_ID_880 = "com.tencent.mm:id/a1u";

	/**红包详情界面显示抢到谁的红包的ID**/
	private static final String FROM_NAME_TEXT_ID_760 = "com.tencent.mm:id/b3p"; //显示抢到谁的红包的text id
	private static final String FROM_NAME_TEXT_ID_780 = "com.tencent.mm:id/b5l"; //显示抢到谁的红包的text id
	private static final String FROM_NAME_TEXT_ID_800 = "com.tencent.mm:id/b72";
	private static final String FROM_NAME_TEXT_ID_821 = "com.tencent.mm:id/b7r";
	private static final String FROM_NAME_TEXT_ID_840 = "com.tencent.mm:id/b8p";
	private static final String FROM_NAME_TEXT_ID_861 = "com.tencent.mm:id/b8w";
	private static final String FROM_NAME_TEXT_ID_880 = "com.tencent.mm:id/baw";


	/**红包详情界面显示抢到的钱数的ID**/
	private static final String MONEY_TEXT_ID_760 = "com.tencent.mm:id/b3t"; //显示抢到的钱的text id
	private static final String MONEY_TEXT_ID_780 = "com.tencent.mm:id/b5p"; //显示抢到的钱的text id
	private static final String MONEY_TEXT_ID_800 = "com.tencent.mm:id/b76";
	private static final String MONEY_TEXT_ID_821 = "com.tencent.mm:id/b7v";
	private static final String MONEY_TEXT_ID_840 = "com.tencent.mm:id/b8t";
	private static final String MONEY_TEXT_ID_861 = "com.tencent.mm:id/b90";
	private static final String MONEY_TEXT_ID_880 = "com.tencent.mm:id/bb0";

	/**拆红包界面“拆”按钮ID**/
	private static final String OPEN_BUTTON_ID_680 = "com.tencent.mm:id/b43";
	private static final String OPEN_BUTTON_ID_700 = "com.tencent.mm:id/b2c";
	private static final String OPEN_BUTTON_ID_760 = "com.tencent.mm:id/b3h";
	private static final String OPEN_BUTTON_ID_780 = "com.tencent.mm:id/b5d";
	private static final String OPEN_BUTTON_ID_800 = "com.tencent.mm:id/b9m";
	private static final String OPEN_BUTTON_ID_821 = "com.tencent.mm:id/b_b";
	private static final String OPEN_BUTTON_ID_840 = "com.tencent.mm:id/ba_";
	private static final String OPEN_BUTTON_ID_861 = "com.tencent.mm:id/bag";
	private static final String OPEN_BUTTON_ID_880 = "com.tencent.mm:id/bdg";

	/**微信聊天界面标题名称ID**/
	private static final String CHAT_UI_TITLE_ID = "com.tencent.mm:id/ces";
	private static final String CHAT_UI_TITLE_ID_680 = "com.tencent.mm:id/ew";
	private static final String CHAT_UI_TITLE_ID_700 = "com.tencent.mm:id/cbo";
	private static final String CHAT_UI_TITLE_ID_760 = "com.tencent.mm:id/cea";
	private static final String CHAT_UI_TITLE_ID_780 = "com.tencent.mm:id/cg_";
	private static final String CHAT_UI_TITLE_ID_800 = "com.tencent.mm:id/ei";
	private static final String CHAT_UI_TITLE_ID_821 = "com.tencent.mm:id/ek";
	private static final String CHAT_UI_TITLE_ID_840 = "com.tencent.mm:id/ey";
	private static final String CHAT_UI_TITLE_ID_861 = "com.tencent.mm:id/ey";//861的这个id和840是一样的
	private static final String CHAT_UI_TITLE_ID_880 = "com.tencent.mm:id/ff";
	public static final String TTTTTT = "aaaaaaaaaaaaaaaaaaaa";
	/**
	 *  6.3.8 对应code为680
	 *  6.3.9对应code为700
	 *  6.3.15对应code为760
	 *  6.3.16对应code为780
	 *  6.3.18对应code为800
	 *  6.3.22对应code为821
	 *  6.3.23对应code为840
	 *  6.3.25对应code为861
	 */

	/**微信输入信息框ID**/
	public static String getWechatEditTextId(int versionCode) {
		String editEextId = null;
		switch (versionCode) {
			case 760:
				editEextId = EDITTEXT_ID_760;
				break;
			case 780:
				editEextId = EDITTEXT_ID_780;
				break;
			case 800:
				editEextId = EDITTEXT_ID_800;
				break;
			case 821:
				editEextId = EDITTEXT_ID_821;
				break;
			case 840:
				editEextId = EDITTEXT_ID_840;
				break;
			case 860:
				editEextId = EDITTEXT_ID_861;
				break;
			case 861:
				editEextId = EDITTEXT_ID_861;
				break;
			case 880:
				editEextId = EDITTEXT_ID_880;
				break;
			default:
				break;
		}
		return editEextId;
	}

	/**微信发送信息按钮ID**/
	public static String getSendButtonId(int versionCode) {
		String sendButtonId = null;
		switch (versionCode) {
			case 760:
				sendButtonId = SEND_BUTTON_ID_760;
				break;
			case 780:
				sendButtonId = SEND_BUTTON_ID_780;
				break;
			case 800:
				sendButtonId = SEND_BUTTON_ID_800;
				break;
			case 821:
				sendButtonId = SEND_BUTTON_ID_821;
				break;
			case 840:
				sendButtonId = SEND_BUTTON_ID_840;
				break;
			case 860:
				sendButtonId = SEND_BUTTON_ID_861;
				break;
			case 861:
				sendButtonId = SEND_BUTTON_ID_861;
				break;
			case 880:
				sendButtonId = SEND_BUTTON_ID_880;
				break;
			default:
				break;
		}
		return sendButtonId;
	}

	/**微信红包详情界面用户名文本框ID**/
	public static String getFromNameTextId(int versionCode) {
		String fromNameTextId = null;
		switch (versionCode) {
			case 760:
				fromNameTextId = FROM_NAME_TEXT_ID_760;
				break;
			case 780:
				fromNameTextId = FROM_NAME_TEXT_ID_780;
				break;
			case 800:
				fromNameTextId = FROM_NAME_TEXT_ID_800;
				break;
			case 821:
				fromNameTextId = FROM_NAME_TEXT_ID_821;
				break;
			case 840:
				fromNameTextId = FROM_NAME_TEXT_ID_840;
				break;
			case 860:
				fromNameTextId = FROM_NAME_TEXT_ID_861;
				break;
			case 861:
				fromNameTextId = FROM_NAME_TEXT_ID_861;
				break;
			case 880:
				fromNameTextId = FROM_NAME_TEXT_ID_880;
				break;
			default:
				break;
		}
		return fromNameTextId;
	}

	/**微信红包详情界面显示多少元的文本框ID**/
	public static String getMoneyTextId(int versionCode) {
		String moneyTextId = null;
		switch (versionCode) {
			case 760:
				moneyTextId = MONEY_TEXT_ID_760;
				break;
			case 780:
				moneyTextId = MONEY_TEXT_ID_780;
				break;
			case 800:
				moneyTextId = MONEY_TEXT_ID_800;
				break;
			case 821:
				moneyTextId = MONEY_TEXT_ID_821;
				break;
			case 840:
				moneyTextId = MONEY_TEXT_ID_840;
				break;
			case 860:
				moneyTextId = MONEY_TEXT_ID_861;
				break;
			case 861:
				moneyTextId = MONEY_TEXT_ID_861;
				break;
			case 880:
				moneyTextId = MONEY_TEXT_ID_880;
				break;
			default:
				break;
		}
		return moneyTextId;
	}

	/**微信拆红包界面“拆”按钮ID**/
	public static String getOpenButtonId(int versionCode) {
		String buttonId = null;
		buttonId = null;

		switch (versionCode) {
			case 680:
				buttonId = OPEN_BUTTON_ID_680;
				break;
			case 700:
				buttonId = OPEN_BUTTON_ID_700;
				break;
			case 760:
				buttonId = OPEN_BUTTON_ID_760;
				break;
			case 780:
				buttonId = OPEN_BUTTON_ID_780;
				break;
			case 800:
				buttonId = OPEN_BUTTON_ID_800;
				break;
			case 821:
				buttonId = OPEN_BUTTON_ID_821;
				break;
			case 840:
				buttonId = OPEN_BUTTON_ID_840;
				break;
			case 860:
				buttonId = OPEN_BUTTON_ID_861;
				break;
			case 861:
				buttonId = OPEN_BUTTON_ID_861;
				break;
			case 880:
				buttonId = OPEN_BUTTON_ID_880;
				break;
			default:
				break;
		}

		return buttonId;
	}

	/**微信聊天界面显示名称的文本框ID**/
	public static String getChatUiTitleId(int versionCode) {
		String chatTitleId = null;
		chatTitleId = CHAT_UI_TITLE_ID;

		switch (versionCode) {
			case 680:
				chatTitleId = CHAT_UI_TITLE_ID_680;
				break;
			case 700:
				chatTitleId = CHAT_UI_TITLE_ID_700;
				break;
			case 760:
				chatTitleId = CHAT_UI_TITLE_ID_760;
				break;
			case 780:
				chatTitleId = CHAT_UI_TITLE_ID_780;
				break;
			case 800:
				chatTitleId = CHAT_UI_TITLE_ID_800;
				break;
			case 821:
				chatTitleId = CHAT_UI_TITLE_ID_821;
				break;
			case 840:
				chatTitleId = CHAT_UI_TITLE_ID_840;
				break;
			case 860:
				chatTitleId = CHAT_UI_TITLE_ID_861;
				break;
			case 861:
				chatTitleId = CHAT_UI_TITLE_ID_861;
				break;
			case 880:
				chatTitleId = CHAT_UI_TITLE_ID_880;
				break;
			default:
				break;
		}

		return chatTitleId;
	}

	//start by huanggq for mm video
	public static final String WECHAT_TIMELINE_UI = "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI";
	public static final String WECHAT_PLAY_UI = "com.tencent.mm.plugin.sns.ui.SnsSightPlayerUI";
	public static final String WECHAT_UPLOAD_UP = "com.tencent.mm.plugin.sns.ui.SightUploadUI";

	public static final String WECHAT_GALLERY_UI = "com.tencent.mm.ui.chatting.gallery.ImageGalleryUI";

	public static final String TIMELINE_COMMENT_PLAYING = "com.tencent.mm:id/ev";

	public static final String TIMELINE_COMMENT_PLAYING_821 = "com.tencent.mm:id/ex";
	public static final String TIMELINE_COMMENT_PLAYING_840 = "com.tencent.mm:id/f8";
	public static final String TIMELINE_COMMENT_PLAYING_861 = "com.tencent.mm:id/f8";
	public static final String TIMELINE_COMMENT_PLAYING_880 = "com.tencent.mm:id/fp";

	public static final String PLAY_UI_ID = "com.tencent.mm:id/ay_";

	public static final String PLAY_UI_ID_821 = "com.tencent.mm:id/bh2";
	public static final String PLAY_UI_ID_840 = "com.tencent.mm:id/bi1";
	public static final String PLAY_UI_ID_861 = "com.tencent.mm:id/bi8";
	public static final String PLAY_UI_ID_880 = "com.tencent.mm:id/bl9";

	public static final String GALLERY_UI_VIDEO = "com.tencent.mm:id/alr";
	public static final String GALLERY_UI_VIDEO_840 = "com.tencent.mm:id/aml";
	public static final String GALLERY_UI_VIDEO_861 = "com.tencent.mm:id/aml";
	public static final String GALLERY_UI_VIDEO_880 = "com.tencent.mm:id/api";

	public static final String GALLERY_UI_VIDEO_EDITOR = "com.tencent.mm:id/a4i";
	public static final String GALLERY_UI_VIDEO_EDITOR_840 = "com.tencent.mm:id/a4t";
	public static final String GALLERY_UI_VIDEO_EDITOR_861 = "com.tencent.mm:id/a4t";
	public static final String GALLERY_UI_VIDEO_EDITOR_880 = "com.tencent.mm:id/a7k";

	public static final String  TIMELINE_COMMENT_EIGHT = "com.tencent.mm:id/f5"; //8级
	public static final String  TIMELINE_COMMENT_EIGHT_821 = "com.tencent.mm:id/f_";
	public static final String  TIMELINE_COMMENT_EIGHT_840 = "com.tencent.mm:id/fk";
	public static final String  TIMELINE_COMMENT_EIGHT_861 = "com.tencent.mm:id/fk";
	public static final String  TIMELINE_COMMENT_EIGHT_880 = "com.tencent.mm:id/g2";

	//start by guojun fix bug#8804 and bug#8807
	public static final String  WECHAT_SHORT_VIDEO_UI_ID = "com.tencent.mm:id/a9";
	public static final String  WECHAT_SHORT_VIDEO_UI_ID_840 = "com.tencent.mm:id/a_";
	public static final String  WECHAT_SHORT_VIDEO_UI_ID_861 = "com.tencent.mm:id/a_";
	public static final String  WECHAT_SHORT_VIDEO_UI_ID_880 = "com.tencent.mm:id/af";
	//end by guojun fix bug#8804 and bug#8807

	//end by huanggq for mm video

}
