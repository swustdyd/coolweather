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
	
	
	List<String> dataList = new ArrayList<String>();//ListView数据列表	
	List<Province> provinceList;//省列表	
	List<City> cityList;//市列表	
	List<Country> countryList;//县列表
	Province selectedProvince;//选中的省份	
	City selectedCity;//选中的市	
	Country selectedCounrty;//选中的县
	int currentLevel;//当前选中的级别
	int lastSelectProvinceIndex = 0;//最近选中的省的下标	
	int lastSelectCityIndex = 0;//最近选中的市的下标	
	int lastSelectCounrtyIndex = 0;//最近选中的县的下标
	boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		//已选择城市，并且不是从WeatherActivity跳转过来，才跳转到WeatherActivity
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
		queryProvinces();//加载省级数据
	}
	
	/**
	 * 查询全国所有省，优先从数据库查询，如果没有查询到结果再去服务器上查询
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
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromService(null, "province");
		}
	}
	
	/**
	 * 查询选中省内所有的市，优先从数据库查询，如果没有查询到结果再去服务器上查询
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
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到结果再去服务器上查询
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
	 * 根据传入的代号和类型从服务器上查询省市县级数据
	 * @param code 省市县代号
	 * @param type 一种类型，代表省、市和县
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
					//通过runOnUiTheread()方法回到主线程处理逻辑
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
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
						Log.e("HttpUtil", "sendHttpRequest Exception: " + e);
					}
				});
			}
		});
	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCancelable(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 捕获back键，根据当前级别判断此时应该返回市列表、省列表、还是直接退出
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTRY) {
			queryCities();
		}else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		}else {
			if (isFromWeatherActivity) {
				//如果是从WeatherActivity跳转过来的，则重新回到WeatherActivity
				Intent intent = new Intent(this, WeatherAcitivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
