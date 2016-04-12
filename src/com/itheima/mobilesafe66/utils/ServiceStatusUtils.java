package com.itheima.mobilesafe66.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceStatusUtils {

	/**
	 * 判断服务是否正在运行
	 * 
	 * @param serviceName
	 * @param ctx
	 * @return
	 */
	public static boolean isServiceRunning(String serviceName, Context ctx) {
		// 活动管理器
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = am.getRunningServices(100);// 获取运行的服务,参数表示最多返回的数量

		for (RunningServiceInfo runningServiceInfo : runningServices) {
			String className = runningServiceInfo.service.getClassName();
			if (className.equals(serviceName)) {
				return true;// 判断服务是否运行
			}
		}

		return false;
	}
}
