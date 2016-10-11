package com.android.redenvelope;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class ActivityEx extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setTitle(getTitle());

        ActionBar actionBarView = getActionBar();
/*        if (actionBarView != null) {
            actionBarView.setBackground(ReflectHelper.Context.getDrawable(this, R.drawable.setting_head));
            actionBarView.setTitleColor(getResources().getColor(R.color.setting_title_color));
            actionBarView.setTitle(getTitle());
        }
        this.showBackKey(true, ReflectHelper.Context.getDrawable(this, R.drawable.back_icon));*/
        Utils.setSystemBarColor(this);
    }
    
   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	if (item.getItemId() == android.R.id.home) {
    		finish();
    		return super.onOptionsItemSelected(item);
    	}
    	return super.onOptionsItemSelected(item);
    }*/

}
