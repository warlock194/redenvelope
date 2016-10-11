package com.android.redenvelope;

//import android.app.ActivityManagerNative;
//import android.app.IActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
//import android.os.ServiceManager;
//import android.os.TopwiseProp;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
//import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;


public class MMShortcutBar {
    public static final int MOVE_OFFSET_FACTOR = 1000;
    private static final String TAG = "huanggq";

    private Context mContext;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatShortcutView = null;

    private View mMini;

    Display mDisplay;
    Handler handler;


    private static final String PATH = "KSightPath";
    private static final String THUM_PATH = "KSightThumbPath";
    private static final String MMPAGENAME = "com.tencent.mm";
    private static final String MMCLASSNAME = "com.tencent.mm.plugin.sns.ui.SightUploadUI";
    int left_x;
    int left_y;
    int clkTime;


    public MMShortcutBar(Context context) {
        mContext = context;
        Log.v(TAG, "MultiTasksBar(Context context)");
        mDisplay = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        Log.v(TAG, "mLayoutParams.horizontalMargin=" + mLayoutParams.horizontalMargin + ",mLayoutParams.verticalMargin=" + mLayoutParams.verticalMargin);

        // 创建操作栏View
        OPEN_CLOSE = true;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(mContext);

        mFloatShortcutView = inflater.inflate(R.layout.multi_task_mm, null);

        //初始化控件
        initAllComponent(mFloatShortcutView);

        handler = new Handler();
/*
        mAm = ActivityManagerNative.getDefault();
        if (mAm == null) {
            Log.i(TAG, "aAm is null");

        }

        mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        if (mWm == null) {
            Log.i(TAG, "mWm is null");
        }*/

    }


    private void initAllComponent(View view) {

        //mPanel.setOnTouchListener(mOnTouchListener1)
        mMini = view.findViewById(R.id.mini);
        mMini.setOnClickListener(mClickListener);

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();

            if (id == R.id.mini) {
                onWork();
            }
        }
    };

    public void onOpen(int x, int y, int clickTime) {
        left_x = x;
        left_y = y;
        clkTime = clickTime;
        sendOpMessage(MSG_OPEN);
    }


    private void onWork() {
        sendOpMessage(MSG_WORK);
    }

    private void work() {

        clkTime = clkTime - 1;
        /*if (mWm != null && mAm != null) {
            Rect rect = new Rect();
            rect.set(left_x, left_y - 100, left_x + 50, left_y - 50);
            AccessibilityHelper.perfermTouch(rect, mWm, mAm);
            if (clkTime > 0) {
                onWork();
            }
        }*/

    }


    public void onClose() {
        sendOpMessage(MSG_CLOSE);
    }

    private boolean OPEN_CLOSE;
    private final int MSG_OPEN = 100;
    private final int MSG_CLOSE = 101;
    private final int MSG_WORK = 102;

    private void sendOpMessage(int msg) {
        mMoveHandler.removeMessages(msg);

        Message message = new Message();
        message.what = msg;
        mMoveHandler.sendMessage(message);
    }

    private Handler mMoveHandler = new Handler() {
        public void handleMessage(Message msg) {
            WindowManager wm;

            if (msg.what != MSG_OPEN && mFloatShortcutView == null) {
                return;
            }

            if ( true) {
                return;
            }
//            Log.d(TAG," ---" + TopwiseProp.getDefaultSettingBoolean("support_mm_video_share",true));
            switch (msg.what) {
                case MSG_OPEN:
                    Log.v(TAG, "handleMessage : MSG_OPEN ");
                    mMini.setVisibility(View.VISIBLE);
                    wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    mLayoutParams.x = left_x / 2;
                    mLayoutParams.y = left_y;
                    if (OPEN_CLOSE) {
                        wm.addView(mFloatShortcutView, mLayoutParams);
                    } else {
                        wm.updateViewLayout(mFloatShortcutView, mLayoutParams);
                    }
                    OPEN_CLOSE = false;
                    break;
                case MSG_CLOSE:
                    if (!OPEN_CLOSE) {
                        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(mFloatShortcutView);
                        Log.v(TAG, "handleMessage : MSG_CLOSE mFloatShortcutView =" + mFloatShortcutView);
                        OPEN_CLOSE = true;
                    }
                    break;
                case MSG_WORK:
                    work();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
