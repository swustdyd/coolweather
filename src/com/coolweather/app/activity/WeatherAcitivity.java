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

	LinearLayout weatherInfoLayout;//������������
	TextView cityName;//��ʾ������
	TextView publish;//��ʾ����ʱ��
	TextView weatherDesp;//��ʾ����������Ϣ
	TextView temp1;//��ʾ����1
	TextView temp2;//��ʾ����2
	TextView currentDate;//��ʾ��ǰ����
	Button switchCity;//�л����а�ť
	Button refreshWeather;//���µ�ǰ��������
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ�����ؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityName = (TextView) findViewById(R.id.city_name);
		publish = (TextView) findViewById(R.id.pubilsh_text);
		weatherDesp = (TextView) findViewById(R.id.weather_info);
		temp1 = (TextView) findViewById(R.id.temp1);
		temp2 = (TextView) findViewById(R.id.temp2);
		currentDate = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		//�����л����а�ť�����¼�
		switchCity.setOnClickListener(this);
		//���ø���������ť�����¼�
		refreshWeather.setOnClickListener(this);
		//��ȡ���͹������ؼ�����
		String CountryCode = getIntent().getStringExtra("countryCode");
		if (!TextUtils.isEmpty(CountryCode)) {
			//���ؼ�����ʱ��ȥ��ѯ����
			publish.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			queryWeatherCode(CountryCode);
		}else {
			//û���ؼ����ž�ֱ����ʾ��������
			showWeather();
		}
	}

	/**
	 * �����ؼ����Ų�ѯ��Ӧ��������
	 * @param countryCode �ؼ�����
	 */
	private void queryWeatherCode(String countryCode) {
		String address = "http://m.weather.com.cn/data5/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");
	}
	
	/**
	 * �����������Ų�ѯ����
	 * @param weatherCode ��������
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/adat/cityinfo/" + weatherCode +".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * ���ݴ���ĵ�ַ������ȥ��������ѯ�������Ż�������Ϣ
	 * @param address
	 * @param type
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//�ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					//������������ص�������Ϣ
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
						publish.setText("ͬ��ʧ��");
						Log.e("WeatherAcitivity", "sendHttpRequest" + e);
					}
				});
			}
		});
	}

	/**
	 * ��SharePreference�ļ��ж�ȡ�洢��������Ϣ������ʾ��������
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
		case R.id.switch_city://�л����а�ť��Ӧ�¼�����
			Intent intent = new Intent(WeatherAcitivity.this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather://����������ť��Ӧ�¼�����
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			publish.setText("ͬ����...");
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
