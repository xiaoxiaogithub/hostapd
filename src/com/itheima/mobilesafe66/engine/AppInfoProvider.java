package com.itheima.mobilesafe66.engine;

import java.util.ArrayList;
import java.util.List;

import com.itheima.mobilesafe66.domain.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * 应用信息提供者
 * 
 */
public class AppInfoProvider {

	/**
	 * 获取已安装应用
	 */
	public static ArrayList<AppInfo> getIntalledApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		List<PackageInfo> installedPackages = pm.getInstalledPackages(0);// 获取所有已安装的包

		ArrayList<AppInfo> list = new ArrayList<AppInfo>();
		for (PackageInfo packageInfo : installedPackages) {
			AppInfo info = new AppInfo();
			String packageName = packageInfo.packageName;// 包名
			ApplicationInfo applicationInfo = packageInfo.applicationInfo;// 应用信息
			String name = applicationInfo.loadLabel(pm).toString();// 应用名称
			Drawable icon = applicationInfo.loadIcon(pm);// 应用图标

			int uid = applicationInfo.uid;//当前应用的标识
			
			info.packageName = packageName;
			info.name = name + uid;
			info.icon = icon;

			// 状态机, 通过01状态来表示是否具备某些属性和功能
			int flags = applicationInfo.flags;// 获取应用标记
			if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
				// 安装在sdcard
				info.isRom = false;
			} else {
				// 安装在手机内存
				info.isRom = true;
			}

			if ((flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
				// 系统应用
				info.isUser = false;
			} else {
				// 用户应用
				info.isUser = true;
			}

			list.add(info);
		}

		return list;
	}
}
