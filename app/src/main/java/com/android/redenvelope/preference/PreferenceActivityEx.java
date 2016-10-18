package com.android.redenvelope.preference;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.android.redenvelope.R;
import com.android.redenvelope.Utils;

public class PreferenceActivityEx extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initActionBar();
        setTitle(getResources().getString(R.string.app_title_name));
        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

/*        ActionBarView actionBarView = getActionBarView();
        if (actionBarView != null) {
            actionBarView.setBackground(ReflectHelper.Context.getDrawable(this, R.drawable.setting_head));
            actionBarView.setTitleColor(getResources().getColor(R.color.setting_title_color));
            actionBarView.setTitle(getResources().getString(R.string.app_title_name));
        }
        this.showBackKey(true, ReflectHelper.Context.getDrawable(this, R.drawable.back_icon));*/
        Utils.setSystemBarColor(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	if (item.getItemId() == android.R.id.home) {
    		finish();
    		return super.onOptionsItemSelected(item);
    	}
    	return super.onOptionsItemSelected(item);
    }

}
