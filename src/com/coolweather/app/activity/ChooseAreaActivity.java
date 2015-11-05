package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 
 * @author DYD
 *
 */
public class ChooseAreaActivity extends Activity{

	ListView listView;
	TextView titleText;
	ArrayAdapter<String> adapter;
	CoolWeatherDB coolWeatherDB;
	ProgressDialog progressDialog;
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	
	List<String> dataList = new ArrayList<String>();//ListView�����б�	
	List<Province> provinceList;//ʡ�б�	
	List<City> cityList;//���б�	
	List<Country> countryList;//���б�
	Province selectedProvince;//ѡ�е�ʡ��	
	City selectedCity;//ѡ�е���	
	Country selectedCounrty;//ѡ�е���
	int currentLevel;//��ǰѡ�еļ���
	int lastSelectProvinceIndex = 0;//���ѡ�е�ʡ���±�	
	int lastSelectCityIndex = 0;//���ѡ�е��е��±�	
	int lastSelectCounrtyIndex = 0;//���ѡ�е��ص��±�
	boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		//��ѡ����У����Ҳ��Ǵ�WeatherActivity��ת����������ת��WeatherActivity
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherAcitivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					lastSelectProvinceIndex = index;
					queryCities();
				}else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					lastSelectCityIndex = index;
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTRY) {
					String countryCode = countryList.get(index).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherAcitivity.class);
					intent.putExtra("countryCode", countryCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();//����ʡ������
	}
	
	/**
	 * ��ѯȫ������ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ�������ȥ�������ϲ�ѯ
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(lastSelectProvinceIndex);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromService(null, "province");
		}
	}
	
	/**
	 * ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ�������ȥ�������ϲ�ѯ
	 */
	protected void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() >0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(lastSelectCityIndex);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else {
			queryFromService(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ�������ȥ�������ϲ�ѯ
	 */
	protected void queryCounties() {
		countryList = coolWeatherDB.loadCountries(selectedCity.getId());
		if (countryList.size() > 0) {
			dataList.clear();
			for (Country country : countryList) {
				dataList.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(lastSelectCounrtyIndex);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
		}else {
			queryFromService(selectedCity.getCityCode(), "country");
		}
	}
	
	/**
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ���ؼ�����
	 * @param code ʡ���ش���
	 * @param type һ�����ͣ�����ʡ���к���
	 */
	void queryFromService(final String code, final String type){
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://m.weather.com.cn/data5/city" + code + ".xml";
		}else {
			address = "http://m.weather.com.cn/data5/city.xml";
		}
		showProgressDialog();
		Log.d("ChooseAreaActivity", "queryFromService threadId is " + Thread.currentThread().getId());
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesRespose(coolWeatherDB, response);
				}else if ("city".equals(type)) {
					result = Utility.handleCitiesRespose(coolWeatherDB, response, selectedProvince.getId());
				}else if ("country".equals(type)) {
					result = Utility.handleCountriesRespose(coolWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					//ͨ��runOnUiTheread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("country".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
						Log.e("HttpUtil", "sendHttpRequest Exception: " + e);
					}
				});
			}
		});
	}

	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCancelable(false);
		}
		progressDialog.show();
	}
	
	/**
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * ����back�������ݵ�ǰ�����жϴ�ʱӦ�÷������б�ʡ�б�����ֱ���˳�
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTRY) {
			queryCities();
		}else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		}else {
			if (isFromWeatherActivity) {
				//����Ǵ�WeatherActivity��ת�����ģ������»ص�WeatherActivity
				Intent intent = new Intent(this, WeatherAcitivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
