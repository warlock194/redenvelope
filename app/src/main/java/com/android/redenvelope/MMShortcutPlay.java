package com.android.redenvelope;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.android.redenvelope.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MMShortcutPlay {
    public static final int MOVE_OFFSET_FACTOR = 1000;
    private static final String TAG = "huanggq";

    private Context mContext;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatShortcutView = null;

    private View mMini;

    private float mHorizontalMargin = 0.45f;
    private float mVerticalMargin = 0.8f;

    private String mPath;
    private String mThumb_path;

    Display mDisplay;
    Handler handler;
    private static final String PATH = "KSightPath";
    private static final String THUM_PATH = "KSightThumbPath";
    private static final String MMPAGENAME = "com.tencent.mm";
    private static final String MMCLASSNAME = "com.tencent.mm.plugin.sns.ui.SightUploadUI";

    public MMShortcutPlay(Context context) {
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
        mLayoutParams.horizontalMargin = mHorizontalMargin;
        mLayoutParams.verticalMargin = mVerticalMargin;
        Log.v(TAG, "mLayoutParams.horizontalMargin=" + mLayoutParams.horizontalMargin + ",mLayoutParams.verticalMargin=" + mLayoutParams.verticalMargin);

        // 创建操作栏View
        OPEN_CLOSE = true;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mFloatShortcutView = inflater.inflate(R.layout.multi_task_play, null);

        //初始化控件
        initAllComponent(mFloatShortcutView);
        handler = new Handler();
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

    public void onOpen() {
        sendOpMessage(MSG_OPEN);
    }


    private void onWork() {
        sendOpMessage(MSG_WORK);
    }


    // 从SD卡中读取数据
    private void readSDcardpath() {
        StringBuffer str = new StringBuffer();
        try {
            // 判断是否存在SD
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory()
                        .getCanonicalPath() + "/mmvideo.txt");
                // 判断是否存在该文件
                if (file.exists()) {
                    // 打开文件输入流
                    FileInputStream fileR = new FileInputStream(file);
                    BufferedReader reads = new BufferedReader(
                            new InputStreamReader(fileR));

                    String st = null;

                    while ((st = reads.readLine()) != null) {
                        //start by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
                        if (mUserId ==0)
                            mPath = st;
                        //end by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
                    }
                    fileR.close();
                } else {

                }
            } else {

            }
        } catch (Exception e) {

        }

        Log.i("huanggq", "readSDcardpath mPth: " + mPath);

    }


    private void readSDcardpathThumb() {

        try {

            if (mPath != null) {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath,
                        MediaStore.Images.Thumbnails.MINI_KIND);

                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    Log.i("huanggq", "MicroMsg write thumb file ");
                    File file = Environment.getExternalStorageDirectory();
                    File jpgFile = new File(file, "thumb.jpg");
                    if (jpgFile.exists()) {
                        jpgFile.delete();
                    }
                    java.io.FileOutputStream out = new java.io.FileOutputStream(file.getCanonicalPath()
                            + "/thumb.jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    mThumb_path = jpgFile.getAbsolutePath();

                    Log.i("huanggq", "mThumb_path: " + mThumb_path);

                    out.close();

                }

            }

        }catch (Exception e) {

        }

    }

    private void work() {


        readSDcardpath();
        readSDcardpathThumb();

        if (mThumb_path == null) {
            mThumb_path = mPath;
        }

        if (mPath != null && mThumb_path != null) {

            try {
                Intent intent = new Intent();
                intent.setClassName(MMPAGENAME, MMCLASSNAME);
                intent.putExtra(PATH, mPath);
                intent.putExtra(THUM_PATH, mThumb_path);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent/*, new UserHandle(mUserId)*/);
                Log.d("warlock", "mpath = " + mPath +"   mUserid  = " + mUserId);
            } catch (Exception e) {
                Log.i("huanggq", "startActivityAsUser e " + e.toString());
            }
        }
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
            switch (msg.what) {
                case MSG_OPEN:
                    if (OPEN_CLOSE) {
                        Log.v(TAG, "handleMessage : MSG_OPEN ");
                        mMini.setVisibility(View.VISIBLE);
                        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                        wm.addView(mFloatShortcutView, mLayoutParams);
                        OPEN_CLOSE = false;
                    }
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
    
    //start by guojun topwise 2016.8.4  // FIXME: 16-8-4  bug#9197
    public void setPath(String path){
        mPath = path;
    }

    private int mUserId = 0;
    public void setUserId(int id){
        mUserId = id;
    }
    //end by guojun topwise 2016.8.4   // FIXME: 16-8-4  bug#9197
}
