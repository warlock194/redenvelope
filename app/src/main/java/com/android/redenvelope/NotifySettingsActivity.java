package com.android.redenvelope;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;

import com.android.redenvelope.preference.PreferenceActivityEx;

public class NotifySettingsActivity extends PreferenceActivityEx
		implements OnPreferenceChangeListener{

	private static final String KEY_NOTIFY_SOUND = "key_notify_sound";
	private static final String KEY_NOTIFY_VIBRATE = "key_notify_vibrate";
	private static final String KEY_NOTIFY_NIGHT = "key_notify_night_enable";
	
	private SwitchPreference mSwitchSound;
	private SwitchPreference mSwitchVibrate;
	private SwitchPreference mSwitchNight;
	private Config mConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.notify_settings);
		setTitle(R.string.notify_settings);
		
		mSwitchSound = (SwitchPreference) findPreference(KEY_NOTIFY_SOUND);
		mSwitchSound.setOnPreferenceChangeListener(this);
		mSwitchVibrate = (SwitchPreference) findPreference(KEY_NOTIFY_VIBRATE);
		mSwitchVibrate.setOnPreferenceChangeListener(this);
		mSwitchNight = (SwitchPreference) findPreference(KEY_NOTIFY_NIGHT);
		mSwitchNight.setOnPreferenceChangeListener(this);
		
		mConfig = Config.getConfig(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		initActivity();
	}

	private void initActivity() {
		// TODO Auto-generated method stub
		if (null == mConfig)
			return;
		
		boolean enableSound = mConfig.isNotifySound();
		mSwitchSound.setChecked(enableSound);
		
		boolean enableVibrate = mConfig.isNotifyVibrate();
		mSwitchVibrate.setChecked(enableVibrate);
		
		boolean enableNight = mConfig.isNotifyNight();
		mSwitchNight.setChecked(enableNight);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		// TODO Auto-generated method stub
		String keyValues = preference.getKey();
		boolean enable = (Boolean) value;
		if (KEY_NOTIFY_SOUND.equals(keyValues)) {
			mSwitchSound.setChecked(enable);
			mConfig.setNotifySound(enable);
		} else if (KEY_NOTIFY_VIBRATE.equals(keyValues)) {
			mSwitchVibrate.setChecked(enable);
			mConfig.setNotifyVibrate(enable);
		} else if (KEY_NOTIFY_NIGHT.equals(keyValues)) {
			mSwitchNight.setChecked(enable);
			mConfig.setNotifyNight(enable);
		}
		
		return true;
	}
}
