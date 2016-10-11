package com.android.redenvelope;

import android.content.Context;
import android.content.Intent;

/**
 * Created by topwise on 16-7-2.
 */
public class MMVideoManager {

    private static final String TAG = "MMVideoManager";
    private static final int START = 1000;
    private static final int STOP = START + 1;
    private static final int LOAD = START + 2;

    private String lastPath;
    private Context mContext;

    public void dostart(int x, int y, int clickTime) {
        Intent intent = new Intent();
        intent.putExtra("shortcut_x", x);
        intent.putExtra("shortcut_y", y);
        intent.putExtra("clicktime", clickTime);
        intent.setAction(MMShortcutService.CMD_FLOATSHORTCUT_OPEN);
        intent.setClass(mContext, MMShortcutService.class);
        mContext.startService(intent);
    }

    public void dostartPlayFloat() {
        Intent intent = new Intent();
        intent.setAction(MMShortcutService.CMD_FLOATSHORTCUT_OPEN);
        intent.putExtra("shortcut_play", true);
        intent.setClass(mContext, MMShortcutService.class);
        mContext.startService(intent);
    }

    public void dostopFloat() {
        Intent intent = new Intent();
        intent.setAction(MMShortcutService.CMD_FLOATSHORTCUT_CLOSE);
        intent.putExtra("shortcut_play", true);
        intent.setClass(mContext, MMShortcutService.class);
        mContext.startService(intent);
    }

    public void dostop() {
        Intent intent = new Intent();
        intent.setAction(MMShortcutService.CMD_FLOATSHORTCUT_CLOSE);
        intent.setClass(mContext, MMShortcutService.class);
        mContext.startService(intent);

    }

    public MMVideoManager(Context context) {
        mContext = context;
    }

    public void start() {

    }

    public void stop() {

    }

}
