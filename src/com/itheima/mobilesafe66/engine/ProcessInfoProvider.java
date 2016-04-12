package com.itheima.mobilesafe66.engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.domain.ProcessInfo;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

/**
 * 进程相关信息提供者
 * 
 * @author Kevin
 * 
 */
public class ProcessInfoProvider {

	/**
	 * 获取正在运行的进程集合
	 * 
	 * @param ctx
	 */
	public static ArrayList<ProcessInfo> getRunnningProcesses(Context ctx) {
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = am
				.getRunningAppProcesses();// 获取运行中进程集合

		PackageManager pm = ctx.getPackageManager();
		ArrayList<ProcessInfo> list = new ArrayList<ProcessInfo>();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			ProcessInfo info = new ProcessInfo();
			String packageName = runningAppProcessInfo.processName;// 包名
			info.packageName = packageName;

			int pid = runningAppProcessInfo.pid;// 进程id

			android.os.Debug.MemoryInfo[] processMemoryInfo = am
					.getProcessMemoryInfo(new int[] { pid });// 根据pid返回内存信息

			long memory = processMemoryInfo[0].getTotalPrivateDirty() * 1024;// 获取当前进程占用内存大小
																				// 单位是kb
			info.memory = memory;
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(
						packageName, 0);// 根据包名获取相关应用的信息
				String name = applicationInfo.loadLabel(pm).toString();
				Drawable icon = applicationInfo.loadIcon(pm);

				int flags = applicationInfo.flags;
				if ((flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
					// 系统进程
					info.isUser = false;
				} else {
					// 用户进程
					info.isUser = true;
				}

				info.name = name;
				info.icon = icon;
			} catch (NameNotFoundException e) {
				// 某些系统进程没有名称和图标,会走此异常
				info.name = info.packageName;
				info.icon = ctx.getResources().getDrawable(
						R.drawable.system_default);
				info.isUser = false;
				e.printStackTrace();
			}

			list.add(info);
		}

		return list;
	}

	/**
	 * 获取运行中进程数量
	 */
	public static int getRunningProcessNum(Context ctx) {
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = am
				.getRunningAppProcesses();// 获取运行中进程集合
		return runningAppProcesses.size();
	}

	/**
	 * 获取剩余内存
	 */
	public static long getAvailMemory(Context ctx) {
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);

		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);// 获取内存信息

		return outInfo.availMem;
	}

	/**
	 * 获取总内存
	 */
	public static long getTotalMemory(Context ctx) {
		// 此方法不兼容api16一下
		// ActivityManager am = (ActivityManager) ctx
		// .getSystemService(Context.ACTIVITY_SERVICE);
		//
		// MemoryInfo outInfo = new MemoryInfo();
		// am.getMemoryInfo(outInfo);// 获取内存信息
		//
		// return outInfo.totalMem;
		// 为了解决版本兼容问题, 可以读取/proc/meminfo文件中第一行, 获取总内存大小
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/meminfo"));
			String readLine = reader.readLine();// 读取第一行内容

			char[] charArray = readLine.toCharArray();
			StringBuffer sb = new StringBuffer();
			for (char c : charArray) {
				if (c >= '0' && c <= '9') {// 判断是否是数字
					sb.append(c);
				}
			}

			String total = sb.toString();// 单位kb,需要转成字节
			return Long.parseLong(total) * 1024;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * 清理后台所有进程
	 * 
	 * @param ctx
	 */
	public static void killAll(Context ctx) {
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = am
				.getRunningAppProcesses();

		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			String packageName = runningAppProcessInfo.processName;

			if (packageName.equals(ctx.getPackageName())) {
				continue;
			}
			am.killBackgroundProcesses(packageName);
		}
	}
}
