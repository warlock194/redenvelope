package com.android.redenvelope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


public class UpdateManager {
	private static final String TAG = "UpdateManager";
	private static final String APK_NAME = "RedEnvelope.apk";
	private Context mContext;
	private String newVersionName;
	private Long newVersionCode;
	private ProgressDialog mProgressDialog;
	private boolean isAutoCheck = true;
	private boolean isLasterUpdate = false;
	private boolean isOnSettingActivity = false;
	private AlertDialog mDialog;
	
	public UpdateManager(Context context) {
		mContext = context;
	}
	
	public void setAutoCheck(boolean isAuto) {
		isAutoCheck = isAuto;
	}
	
	public void checkUpdate() {
		new checkNewVersionAsyncTask().execute();
	}
	
	public boolean isLasterUpdate() {
		return isLasterUpdate;
	}
	
	public void setOnSettingActivity(boolean enable) {
		isOnSettingActivity = enable;
	}
	
	class checkNewVersionAsyncTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if (postCheckNeweVersionToServer()) {
				long curVersionCode = getVersionCode(mContext);
				if (newVersionCode > curVersionCode) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (isOnSettingActivity) {
				if (result) {
					showNoticeDialog();
				} else {
					if (!isAutoCheck) {
						Toast.makeText(mContext, mContext.getResources().getString(R.string.check_update_toast), 
								Toast.LENGTH_SHORT).show();
					}
				}
			}
			super.onPostExecute(result);
		}

	}
	
	private void showNoticeDialog() {
		// TODO Auto-generated method stub
		if (null == mDialog) {
			String curVersionName = getVersionName(mContext);
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setTitle(R.string.soft_update_title);
			builder.setMessage(mContext.getResources().getString(R.string.soft_update_info, curVersionName, newVersionName));
			// 更新
			builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					// 显示下载对话框
					showDownloadDialog();
				}
			});
			// 稍后更新
			builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					isLasterUpdate = true;
				}
			});
			mDialog = builder.create();
			mDialog.show();
		} else {
			if (!mDialog.isShowing()) {
				mDialog.show();
			}
		}
	}
	
	private void showDownloadDialog() {
		mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setTitle(R.string.update_progress_title);
		if (HttpHelper.IS_ALI_SIGNED) {
			downloadApk(HttpHelper.UPDATESOFTADDRESS);
		}else {
			downloadApk(HttpHelper.UPDATESOFTADDRESS_UNSIGNED);
		}
	}
	
	private void downloadApk(final String url) {
		mProgressDialog.show();
		new Thread() {
	        public void run() {  
	            HttpClient client = new DefaultHttpClient();
	            HttpGet get = new HttpGet(url);
	            HttpResponse response;
	            try {  
	                response = client.execute(get);  
	                HttpEntity entity = response.getEntity();
	                long length = entity.getContentLength();  
	                
	                InputStream is = entity.getContent();  
	                FileOutputStream fileOutputStream = null;  
	                if (is != null) {  
	                    File file = new File(  
	                            Environment.getExternalStorageDirectory(),  
	                            APK_NAME);  
	                    fileOutputStream = new FileOutputStream(file);  
	                    byte[] buf = new byte[1024];  
	                    int ch = -1;  
	                    int count = 0;  
	                    while ((ch = is.read(buf)) != -1) {  
	                        fileOutputStream.write(buf, 0, ch);  
	                        count += ch;  
	                        if (length > 0) {  
	                        	mProgressDialog.setProgress(count);
	                        }  
	                    }
	                }  
	                fileOutputStream.flush();  
	                if (fileOutputStream != null) {  
	                    fileOutputStream.close();  
	                }  
	            } catch (ClientProtocolException e) {
	                e.printStackTrace();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	            installApk();
	        }  
	    }.start();
	}
	
	private void installApk() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
            	mProgressDialog.cancel();
                updateApk();
            }
        });
	}	
	
	private Boolean postCheckNeweVersionToServer()
	{
		StringBuilder builder = new StringBuilder();
		JSONArray jsonArray = null;
		try {
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new BasicNameValuePair("action", "checkNewVersion"));
			builder = HttpHelper.postToServer(list);
			if (null != builder) {
				Log.e(TAG, builder.toString());
				jsonArray = new JSONArray(builder.toString());
				if (jsonArray.length()>0) {
					if (jsonArray.getJSONObject(0).getInt("id") == 1) {
						newVersionName = jsonArray.getJSONObject(0).getString("versionName");
						newVersionCode = jsonArray.getJSONObject(0).getLong("versionCode");
						Log.i(TAG, "newVersionName : " + newVersionName + "  newVersionCode : " + newVersionCode);
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			Log.e(TAG, "postCheckNeweVersionToServer exception : " + e.getMessage());
			newVersionName = "";
			newVersionCode = (long) -1;
			return false;
		}
	}
	
	private int getVersionCode(Context context) {
		int verCode = -1;
	    try {
	    	verCode = context.getPackageManager().getPackageInfo(
	        	"com.android.redenvelope", 0).versionCode;
	    } catch (NameNotFoundException e) {
	        Log.e(TAG, e.getMessage());
	    }
	    Log.i(TAG, "current versionCode : " + verCode);
	    return verCode;
	}
	   
	public String getVersionName(Context context) {
	 	String verName = "";
	    try {
	    	verName = context.getPackageManager().getPackageInfo(
	        	"com.android.redenvelope", 0).versionName;
	    } catch (NameNotFoundException e) {
	        Log.e(TAG, e.getMessage());
	    }
	    Log.i(TAG, "current versionName : " + verName);
	    return verName;   
	}
	
	private void updateApk() {
        try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(Environment
			        .getExternalStorageDirectory(), APK_NAME)),
			        "application/vnd.android.package-archive");
			mContext.startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "updateApk exception : " + e.toString());
			Toast.makeText(mContext, mContext.getResources().getString(R.string.install_apk_error), 
					Toast.LENGTH_SHORT).show();
		}
    }
}
