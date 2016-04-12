package com.itheima.mobilesafe66.service;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.itheima.mobilesafe66.utils.PrefUtils;

/**
 * 手机定位服务
 * 
 * @author Kevin
 * 
 */
public class LocationService extends Service {

	private LocationManager mLM;
	private MyListener mListener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLM = (LocationManager) getSystemService(LOCATION_SERVICE);

		// 初始化标准
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);// 设置精度标准
		criteria.setCostAllowed(true);// 允许花费流量

		String bestProvider = mLM.getBestProvider(criteria, true);// 获取当前最合适的位置提供者,
																	// 参1:标准,参2:是否可用

		System.out.println("best provider:" + bestProvider);
		mListener = new MyListener();
		mLM.requestLocationUpdates(bestProvider, 0, 0, mListener);
	}

	class MyListener implements LocationListener {

		// 位置发生变化
		@Override
		public void onLocationChanged(Location location) {
			String j = "j:" + location.getLongitude();
			String w = "w:" + location.getLatitude();
			String accuracy = "accuracy:" + location.getAccuracy();

			String result = j + "\n" + w + "\n" + accuracy;

			// 发送经纬度给安全号码
			String phone = PrefUtils.getString("safe_phone", "",
					getApplicationContext());
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage(phone, null, result, null, null);

			stopSelf();// 服务自杀的方法
		}

		// 状态发生变化
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			System.out.println("onStatusChanged");
		}

		// 用户打开GPS
		@Override
		public void onProviderEnabled(String provider) {
			System.out.println("onProviderEnabled");
		}

		// 用户关闭GPS
		@Override
		public void onProviderDisabled(String provider) {
			System.out.println("onProviderDisabled");
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 停止位置监听
		mLM.removeUpdates(mListener);
		mListener = null;
	}
}
