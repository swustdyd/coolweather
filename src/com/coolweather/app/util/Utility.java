package com.coolweather.app.util;


import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

public class Utility {
	
	/**
	 * ����������������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesRespose(CoolWeatherDB coolWeatherDB, String response){
		Log.d("Utility", "handleProvincesRespose threadId is " + Thread.currentThread().getId());
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String each : allProvinces) {
					String[] provinceData = each.split("\\|");
					Province province = new Province();
					province.setProvinceCode(provinceData[0]);
					province.setProvinceName(provinceData[1]);
					//�����ݴ洢��Province��
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ����������������ص��м�����
	 */
	public synchronized static boolean handleCitiesRespose(CoolWeatherDB coolWeatherDB,
			String response, int provinceId){
		Log.d("Utility", "handleCitiesRespose threadId is " + Thread.currentThread().getId());
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String each : allCities) {
					String[] cityData = each.split("\\|");
					City city = new City();
					city.setCityCode(cityData[0]);
					city.setCityName(cityData[1]);
					city.setProvinceId(provinceId);
					//�����ݴ洢��City��
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ����������������ص��ؼ�����
	 */
	public synchronized static boolean handleCountriesRespose(CoolWeatherDB coolWeatherDB,
			String response, int cityId){
		Log.d("Utility", "handleCountriesRespose threadId is " + Thread.currentThread().getId());
		if (!TextUtils.isEmpty(response)) {
			String[] allCountries = response.split(",");
			if (allCountries != null && allCountries.length > 0) {
				for (String each : allCountries) {
					String[] countryData = each.split("\\|");
					Country country = new Country();
					country.setCountryCode(countryData[0]);
					country.setCountryName(countryData[1]);
					country.setCityId(cityId);
					//�����ݴ洢��Country��
					coolWeatherDB.saveCountry(country);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �������������ص�JSON���ݣ������������������ݴ洢������
	 */
	public static void handleWeatherResponse(Context context, String response){
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/**
	 * �����������ص�������Ϣ�洢��SharePreference�ļ���
	 * @param context
	 * @param cityName
	 * @param weatherCode
	 * @param temp1
	 * @param temp2
	 * @param weatherDesp
	 * @param publishTime
	 */
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		//��ȡ��ǰ������
		Calendar c = Calendar.getInstance();
		String date = c.get(Calendar.YEAR)+ "��"
				+ (c.get(Calendar.MONTH) + 1) + "��"
				+ c.get(Calendar.DATE) + "��";
		//��������Ϣ��ŵ�����
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", "����" + publishTime + "����");
		editor.putString("current_date", date);
		editor.commit();
	}

}
