package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import android.util.Log;


public class HttpUtil {
	/**
	 * 发送Http请求
	 * @param address Http地址
	 * @param listener HttpCallbackListener回调监听借口，将数据返回
	 */
	public static final void sendHttpRequest(final String address, final HttpCallbackListener listener){
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("HttpUtil", "sendHttpRequest threadId is " + Thread.currentThread().getId());
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line = null;
					while ((line = br.readLine()) != null) {
						response.append(line);
					}
					if (listener != null) {
						//回调onFinish方法
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					//回调onError方法
					listener.onError(e);
				}finally{
					if (connection != null) {
						connection.disconnect();
					}
				}
				
			}
		}).start();
	}

}
