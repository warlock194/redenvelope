package com.android.redenvelope;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class HttpHelper {

	private static final String TAG = "HttpHelper";
	public static final String SERVER_IP="http://www.comio.cn/topwise/";
	public static final String SERVER_ADDRESS=SERVER_IP+"index.php";
	public static final String SERVER_ADDRESS_UNSIGNED=SERVER_IP+"unsigned/index.php";
	public static final String UPDATESOFTADDRESS=SERVER_IP+"patch_dex.jar";
	public static final String UPDATESOFTADDRESS_UNSIGNED=SERVER_IP+"unsigned/RedEnvelope.apk";
	public static final boolean IS_ALI_SIGNED = true;
	public static StringBuilder postToServer(List<NameValuePair> list) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpResponse response = null;
			HttpPost httpost = new HttpPost(SERVER_ADDRESS);
			if (!IS_ALI_SIGNED) {
				httpost = new HttpPost(SERVER_ADDRESS_UNSIGNED);
			}
			StringBuilder builder = new StringBuilder();

			httpost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
			response = httpclient.execute(httpost);

			if (response.getEntity() != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				String s = reader.readLine();
				for (; s != null; s = reader.readLine()) {
					builder.append(s);
				}
			}
				return builder;

		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "postToServer exceprtion : " + e.getMessage());
			return null;
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
}
