package com.coolweather.app.util;

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

}
