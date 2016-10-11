package com.android.redenvelope;

import com.android.redenvelope.preference.PreferenceActivityEx;
import com.android.redenvelope.service.RedEnvelopeService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivityEx
			implements OnPreferenceChangeListener, OnPreferenceClickListener{

	private static final String TAG = "SettingActivity";
	private static final String KEY_ENABLE_WECHAT = "key_enable_wechat";
	private static final String KEY_ENABLE_NOTIFY = "key_enable_notify";
	private static final String KEY_WECHAT_MODE = "key_wechat_mode";
	private static final String KEY_RECEIVE_DELAY = "key_receive_delay";
	private static final String KEY_AUTO_ANSWER = "key_auto_answer";
	private static final String KEY_NOTIFY_SETTINGS = "key_red_envelope_notify_settings";
	private static final String KEY_RED_ENVELOPE_RECORD = "Key_red_envelope_record";
	private static final String KEY_RED_ENVELOPE_UPDATE = "Key_red_envelope_update";
	private static final String KEY_PROMPT = "prompt";
	
	private SwitchPreference mEnableWechat;
	private SwitchPreference mEnableNotify;
	private ListPreference mListWechatMode;
	private ListPreference mListReceiveDelay;
	private ListPreference mListAutoAnswer;
	private Preference mNotifySettings;
	private Preference mRedEnvelopeRecord;
	private Preference mRedEnvelopeUpdate;
	private Preference mPromptPreference;
	private Config mConfig;
	private Dialog mTipsDialog;
	private Dialog mDefinedReplyDialog;
	private UpdateManager mUpdateManager;
	private String mVersionCode;
	private ConnectivityManager manager;
	private static final int SETTINGS_ACCESSIBILITY_SERVER = Menu.FIRST;
	private static final int CHECK_NEW_VERSION = SETTINGS_ACCESSIBILITY_SERVER + 1;
	private static final int SETTINGS_HELP = CHECK_NEW_VERSION + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_pref);
		
		mEnableWechat = (SwitchPreference) findPreference(KEY_ENABLE_WECHAT);
		mEnableWechat.setOnPreferenceChangeListener(this);
		mEnableNotify = (SwitchPreference) findPreference(KEY_ENABLE_NOTIFY);
		mEnableNotify.setOnPreferenceChangeListener(this);
		mListWechatMode = (ListPreference) findPreference(KEY_WECHAT_MODE);
		mListWechatMode.setOnPreferenceChangeListener(this);
		mListReceiveDelay = (ListPreference) findPreference(KEY_RECEIVE_DELAY);
		mListReceiveDelay.setOnPreferenceChangeListener(this);
		mListAutoAnswer = (ListPreference) findPreference(KEY_AUTO_ANSWER);
		mListAutoAnswer.setOnPreferenceChangeListener(this);
		mNotifySettings = findPreference(KEY_NOTIFY_SETTINGS);
		mNotifySettings.setOnPreferenceClickListener(this);
		mRedEnvelopeRecord = findPreference(KEY_RED_ENVELOPE_RECORD);
		mRedEnvelopeRecord.setOnPreferenceClickListener(this);
		mRedEnvelopeUpdate = findPreference(KEY_RED_ENVELOPE_UPDATE);
		mRedEnvelopeUpdate.setOnPreferenceClickListener(this);
		mPromptPreference = findPreference(KEY_PROMPT);

		if (Config.SUPPORT_DELAY_TIME_RANDOM) {
			mListReceiveDelay.setEntries(R.array.receive_delay_entries_random);
			mPromptPreference.setLayoutResource(R.layout.hint_other);
		}

		if (!Config.SETTING_WECHAT_MODE) {
			if (null != mListWechatMode) {
				getPreferenceScreen().removePreference(mListWechatMode);
			}
		}
		
		if (Integer.valueOf(android.os.Build.VERSION.SDK) < 21) {
			//ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE sdk must large or equals 21
			getPreferenceScreen().removePreference(mListAutoAnswer);
			
		}

		if (!Config.SETTING_SHOW_NOTIFY) {
			if (null != mNotifySettings) {
				getPreferenceScreen().removePreference(mNotifySettings);
			}
		}

		if (!Config.SHOW_UPDATE_PREFERENCE) {
			if (null != mRedEnvelopeUpdate) {
				getPreferenceScreen().removePreference(mRedEnvelopeUpdate);
			}
		}
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_RED_ENVELOPE_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_RED_ENVELOPE_SERVICE_CONNECT);
        registerReceiver(envelopeConnectReceiver, filter);

        mUpdateManager = new UpdateManager(SettingActivity.this);
		mVersionCode = mUpdateManager.getVersionName(SettingActivity.this);
		mRedEnvelopeUpdate.setSummary("当前版本 "+mVersionCode);
	}

	private void initActivity() {
		// TODO Auto-generated method stub
		mConfig = Config.getConfig(this);
		if (null != mConfig) {
			boolean enableRedPacket = mConfig.isEnableRedPacket();
			mEnableWechat.setChecked(enableRedPacket);
			
			boolean enableNotify = mConfig.isNotifyAfterGet();
			mEnableNotify.setChecked(enableNotify);
			
			if (Config.SETTING_WECHAT_MODE) {
				int mode = mConfig.getRedPacketMode();
				mListWechatMode.setDefaultValue(mode);
				updateRedPacketModePreference(mode);
			}
			
			int delay = mConfig.getReceiveDelayValue();
			mListReceiveDelay.setDefaultValue(delay);
			updateReceiveDelayPreference(delay);
			
			if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 21) {
				int autoAnser = mConfig.getAutoAnswer();
				String replyStr = Config.getConfig(this).getDefinedAutoReply();
				if (null != replyStr) {
					CharSequence[] definedEntries = this.getResources().getStringArray(R.array.defined_auto_answer_entries);
					int length = definedEntries.length;
					definedEntries[length-2] = replyStr;
					mListAutoAnswer.setEntries(definedEntries);
					mListAutoAnswer.setEntryValues(R.array.defined_auto_answer_values);
				}
				mListAutoAnswer.setDefaultValue(autoAnser);
				updateAutoAnswerPreference(autoAnser);
			}
		}
		if (Config.SUPPORT_UPDATE) {
			mUpdateManager.setOnSettingActivity(true);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initActivity();
		
		if(RedEnvelopeService.isRunning()) {
            if(mTipsDialog != null) {
                mTipsDialog.dismiss();
            }

            if (Config.SUPPORT_UPDATE && !mUpdateManager.isLasterUpdate() && Config.SHOW_UPDATE_PREFERENCE && isNetworkAvailable()) {
	            mUpdateManager.setAutoCheck(true);
	            mUpdateManager.checkUpdate();
            }
        } else {
            showOpenAccessibilityServiceDialog();
        }
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (Config.SUPPORT_UPDATE) {
			mUpdateManager.setOnSettingActivity(false);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (null != envelopeConnectReceiver) {
			unregisterReceiver(envelopeConnectReceiver);
			envelopeConnectReceiver = null;
		}
		mUpdateManager = null;
		mTipsDialog = null;
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object object) {
		// TODO Auto-generated method stub
		String key = preference.getKey();
		Log.i(TAG, "onPreferenceChange key : " + key);
		if (KEY_ENABLE_WECHAT.equals(key)) {
			boolean value = (Boolean) object;
			mEnableWechat.setChecked(value);
			if (null != mConfig) {
				mConfig.setEnableRedPacket(value);
			}
		} else if (KEY_ENABLE_NOTIFY.equals(key)) {
			boolean value = (Boolean) object;
			mEnableNotify.setChecked(value);
			if (null != mConfig) {
				mConfig.setNotifyAfterGet(value);
			}
		} else if (KEY_WECHAT_MODE.equals(key)){
			int value = Integer.parseInt((String) object);
			updateRedPacketModePreference(value);
		} else if (KEY_RECEIVE_DELAY.equals(key)) {
			int value = Integer.parseInt((String) object);
			updateReceiveDelayPreference(value);
		} else if (KEY_AUTO_ANSWER.equals(key)) {
			int value = Integer.parseInt((String) object);
			final CharSequence[] entries = mListAutoAnswer.getEntryValues();
			if (value < entries.length-1) {
				updateAutoAnswerPreference(value);
			} else {
				showUserDefinedReplyDialog(true);
			}
		}
		return true;
	}

	private void updateReceiveDelayPreference(int value) {
		Log.i(TAG, "updateAutoAnswerPreference value = " + value);
		 ListPreference preference = mListReceiveDelay;
	     String summary;
	     if (value < 0) {
	     	// Unsupported value
	        summary = getResources().getString(R.string.none);
	     } else {
	        final CharSequence[] entries = preference.getEntries();
	        final CharSequence[] values = preference.getEntryValues();
	     	if (entries == null || entries.length == 0) {
	        	summary = getResources().getString(R.string.none);
	        } else {
	            if (entries.length != 0 && values.length > value && 0 != value) {
	            	summary = String.valueOf(entries[value]);
	            	//save delay open red envelope times
					if (null != mConfig) {
						if (Config.SUPPORT_DELAY_TIME_RANDOM && (value == (values.length-1))) {
							mConfig.setOpenDelayTime(value);
						} else {
							int delayTime = (int) (Float.parseFloat(summary.replace("s", "")) * 1000); //ms
							mConfig.setOpenDelayTime(delayTime);
						}
					}
	            } else {
	                summary = getResources().getString(R.string.none);
	                if (null != mConfig) {
	            		mConfig.setOpenDelayTime(0);
	            	}
	            }
	        }
	     	
	     }
	     if (null != mConfig) {
	    	 mConfig.setReceiveDelayValue(value);
	     }
	     Log.i(TAG, "updateReceiveDelayPreference summary = " + summary);
	     preference.setSummary(summary); 	          
	}
	
	private void updateAutoAnswerPreference(int value) {
		Log.i(TAG, "updateAutoAnswerPreference value = " + value);
		ListPreference preference = mListAutoAnswer;
	     String summary;
	     if (value < 0) {
	     	// Unsupported value
	        summary = getResources().getString(R.string.none);
	     } else {
	        final CharSequence[] entries = preference.getEntries();
	        final CharSequence[] values = preference.getEntryValues();
	     	if (entries == null || entries.length == 0) {
	        	summary = getResources().getString(R.string.none);
	        } else {
	            if (entries.length != 0 && values.length > value) {
	                summary = String.valueOf(entries[value]);
	            } else {
	                summary = getResources().getString(R.string.none);
	            }
	        }
	     }
	     
	     if (null != mConfig) {
	    	 mConfig.setAutoAnswer(value);
	     }
	     Log.i(TAG, "updateAutoAnswerPreference summary = " + summary);
	     preference.setSummary(summary); 	 
	}
	
	private void updateRedPacketModePreference(int value) {
		ListPreference preference = mListWechatMode;
	     String summary;
	     if (value < 0) {
	     	// Unsupported value
	        summary = getResources().getString(R.string.wx_mode_0);
	     } else {
	        final CharSequence[] entries = preference.getEntries();
	        final CharSequence[] values = preference.getEntryValues();
	     	if (entries == null || entries.length == 0) {
	        	summary = getResources().getString(R.string.wx_mode_0);
	        } else {
	            if (entries.length != 0 && values.length > value) {
	                summary = String.valueOf(entries[value]);
	            } else {
	                summary = getResources().getString(R.string.wx_mode_0);
	            }
	        }
	     }
	     
	     if (null != mConfig) {
	    	 mConfig.setRedPacketMode(value);
	     }
	     Log.i(TAG, "updateAutoAnswerPreference summary = " + summary);
	     preference.setSummary(summary); 	 
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		String keyValues = preference.getKey();
		if (KEY_NOTIFY_SETTINGS.equals(keyValues)) {
			Intent intent = new Intent(this, NotifySettingsActivity.class);
			startActivity(intent);
		} else if (KEY_RED_ENVELOPE_RECORD.equals(keyValues)) {
			Intent intent = new Intent(this, RedEnvelopeRecord.class);
			startActivity(intent);
		} else if (KEY_RED_ENVELOPE_UPDATE.equals(keyValues)) {
			/*if (!isNetworkAvailable()) {
				Toast.makeText(SettingActivity.this,getResources().getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
				return true;
			}
			mUpdateManager.setAutoCheck(false);
			mUpdateManager.checkUpdate();*/
			Toast.makeText(SettingActivity.this, WechatConfig.TTTTTT, Toast.LENGTH_SHORT).show();
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		addOptionsMenuItems(menu);
		return true;
	}
	
	private void addOptionsMenuItems(Menu menu) {
		// TODO Auto-generated method stub
//		menu.add(Menu.NONE, SETTINGS_ACCESSIBILITY_SERVER, 0, R.string.setting_service_button);
		if (Config.SUPPORT_UPDATE) {
		//	menu.add(Menu.NONE, CHECK_NEW_VERSION, 0, R.string.check_new_version);
		}
//		menu.add(Menu.NONE, SETTINGS_HELP, 0, R.string.setting_help);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		if (id == SETTINGS_ACCESSIBILITY_SERVER) {
			openAccessibilityServiceSettings();
		} else if (id == CHECK_NEW_VERSION) {
			mUpdateManager.setAutoCheck(false);
	        mUpdateManager.checkUpdate();
		} 
		return super.onOptionsItemSelected(item);
	}
	
	/** 打开辅助服务的设置*/
    private void openAccessibilityServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private BroadcastReceiver envelopeConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isFinishing()) {
                return;
            }
            String action = intent.getAction();
            Log.d(TAG, "receive action : " + action);
            if(Config.ACTION_RED_ENVELOPE_SERVICE_CONNECT.equals(action)) {
                if (mTipsDialog != null) {
                    mTipsDialog.dismiss();
                }
            } else if(Config.ACTION_RED_ENVELOPE_SERVICE_DISCONNECT.equals(action)) {
//                showOpenAccessibilityServiceDialog();//add by zl topwise for bug[6836]
            }
        }
    };
    
    private void showOpenAccessibilityServiceDialog() {
        if(mTipsDialog != null && mTipsDialog.isShowing()) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_tips_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilityServiceSettings();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.open_service_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.open_service_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAccessibilityServiceSettings();
            }
        });
        builder.setNegativeButton(R.string.dilog_exit, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		});
        builder.setCancelable(false);
        mTipsDialog = builder.show();
    }

	private void showUserDefinedReplyDialog(boolean show) {
		if (show) {
			if (mDefinedReplyDialog != null && mDefinedReplyDialog.isShowing()) {
				return;
			}

			AlertDialog.Builder mDialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
			mDialog.setTitle(R.string.auto_answer_3);
			View view = getLayoutInflater().inflate(R.layout.dialog_defined_reply, null);
			mDialog.setView(view);
			final EditText et = (EditText) view.findViewById(R.id.editReply);
//		et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(32) });
			et.setMaxLines(1);
			et.setSingleLine(true);
			et.setGravity(Gravity.CENTER);
			CharSequence tmp = et.getText();
			if (tmp != null) {
				Selection.setSelection((Spannable) tmp, 0, tmp.length());
			}
			mDialog.setCancelable(false);
			mDialog.setPositiveButton(R.string.defined_reply_ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String replyStr = et.getText().toString().trim();
							if (TextUtils.isEmpty(replyStr)) {
								resetAutoAnswerPreference();
								Toast.makeText(SettingActivity.this, R.string.defined_reply_toast, Toast.LENGTH_SHORT).show();
								return;
							}
							Config.getConfig(SettingActivity.this).setDefinedAutoReply(replyStr);
							updateAndRefreshAutoAnswerPreference(replyStr);
						}
					});
			mDialog.setNeutralButton(R.string.defined_reply_cancle, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					resetAutoAnswerPreference();
				}
			});
			mDefinedReplyDialog = mDialog.show();
			et.postDelayed(new Runnable() {
				@Override
				public void run() {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}, 100);
		} else {
			if (mDefinedReplyDialog != null && mDefinedReplyDialog.isShowing()) {
				mDefinedReplyDialog.dismiss();
			}
			mDefinedReplyDialog = null;
		}
	}

	private void updateAndRefreshAutoAnswerPreference(String replyStr) {
		int value = 0;
		Log.i(TAG, "updateAndRefreshAutoAnswerPreference value = " + value);
		ListPreference preference = mListAutoAnswer;

		final CharSequence[] values = this.getResources().getStringArray(R.array.auto_answer_values);
		mListAutoAnswer.setValueIndex(values.length-1);

		if (null != mConfig) {
			mConfig.setAutoAnswer(values.length-1);
		}
		CharSequence[] definedEntries = this.getResources().getStringArray(R.array.defined_auto_answer_entries);
		int length = definedEntries.length;
		definedEntries[length-2] = replyStr;
		mListAutoAnswer.setEntries(definedEntries);
		mListAutoAnswer.setEntryValues(R.array.defined_auto_answer_values);
		Log.i(TAG, "updateAndRefreshAutoAnswerPreference summary = " + replyStr);
		preference.setSummary(replyStr);
	}

	private void resetAutoAnswerPreference() {
		int value = Config.getConfig(SettingActivity.this).getAutoAnswer();
		mListAutoAnswer.setValueIndex(value);
		updateAutoAnswerPreference(value);
	}

	/**
	 * 检测网络是否连接
	 *
	 * @return
	 */
	private boolean isNetworkAvailable() {
		// 得到网络连接信息
		if(manager == null) {
			manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		// 去进行判断网络是否连接
		if (manager != null && manager.getActiveNetworkInfo() != null) {
			return manager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}
}
