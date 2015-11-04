package com.coolweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

public class Utility {
	
	/**
	 * 解析处理服务器返回的省级数据
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
					//将数据存储到Province表
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析处理服务器返回的市级数据
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
					//将数据存储到City表
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析处理服务器返回的县级数据
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
					//将数据存储到Country表
					coolWeatherDB.saveCountry(country);
				}
				return true;
			}
		}
		return false;
	}

}
