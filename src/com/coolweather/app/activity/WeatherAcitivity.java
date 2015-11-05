package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherAcitivity extends Activity implements OnClickListener{

	LinearLayout weatherInfoLayout;//加载天气布局
	TextView cityName;//显示城市名
	TextView publish;//显示发布时间
	TextView weatherDesp;//显示天气描述信息
	TextView temp1;//显示气温1
	TextView temp2;//显示气温2
	TextView currentDate;//显示当前日期
	Button switchCity;//切换城市按钮
	Button refreshWeather;//更新当前城市天气
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityName = (TextView) findViewById(R.id.city_name);
		publish = (TextView) findViewById(R.id.pubilsh_text);
		weatherDesp = (TextView) findViewById(R.id.weather_info);
		temp1 = (TextView) findViewById(R.id.temp1);
		temp2 = (TextView) findViewById(R.id.temp2);
		currentDate = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		//设置切换城市按钮监听事件
		switchCity.setOnClickListener(this);
		//设置更新天气按钮监听事件
		refreshWeather.setOnClickListener(this);
		//获取传送过来的县级代号
		String CountryCode = getIntent().getStringExtra("countryCode");
		if (!TextUtils.isEmpty(CountryCode)) {
			//有县级代号时就去查询天气
			publish.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			queryWeatherCode(CountryCode);
		}else {
			//没有县级代号就直接显示本地天气
			showWeather();
		}
	}

	/**
	 * 根据县级代号查询对应天气代号
	 * @param countryCode 县级代号
	 */
	private void queryWeatherCode(String countryCode) {
		String address = "http://m.weather.com.cn/data5/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");
	}
	
	/**
	 * 根据天气代号查询天气
	 * @param weatherCode 天气代号
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/adat/cityinfo/" + weatherCode +".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * 根据传入的地址和类型去服务器查询天气代号或天气信息
	 * @param address
	 * @param type
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherAcitivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(final Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publish.setText("同步失败");
						Log.e("WeatherAcitivity", "sendHttpRequest" + e);
					}
				});
			}
		});
	}

	/**
	 * 从SharePreference文件中读取存储的天气信息，并显示到界面上
	 */
	private void showWeather() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		cityName.setText(pref.getString("city_name", ""));
		publish.setText(pref.getString("publish_time", ""));
		weatherDesp.setText(pref.getString("weather_desp", ""));
		temp1.setText(pref.getString("temp1", ""));
		temp2.setText(pref.getString("temp2", ""));
		currentDate.setText(pref.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city://切换城市按钮响应事件代码
			Intent intent = new Intent(WeatherAcitivity.this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather://更新天气按钮响应事件代码
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			publish.setText("同步中...");
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = pref.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
		
	}
}
