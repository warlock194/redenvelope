
package com.android.redenvelope;

import android.app.Activity;
import android.view.WindowManager;


public final class Utils {

     /**
     * The opacity level of a disabled icon.
     */
    public static final float DISABLED_ALPHA = 0.4f;



    public static void setSystemBarColor(Activity activity) {
        setSystemBarColor(activity, activity.getResources().getColor(R.color.setting_header_color),
                activity.getResources().getBoolean(R.bool.setting_dark_mode));
    }

    public static void setSystemBarColor(Activity activity, int color, boolean darkMode) {
    }

}
