package com.itheima.mobilesafe66.global;

import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.os.Environment;

/**
 * 自定义Application
 * 
 * @author Kevin
 * @date 2015-8-5
 */
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// 设置未捕获异常的处理器
		Thread.setDefaultUncaughtExceptionHandler(new MyHandler());
	}

	class MyHandler implements UncaughtExceptionHandler {

		// 一旦有未捕获的异常,就会回调此方法
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			System.out.println("发现一个未处理的异常, 但是被哥捕获了...");
			ex.printStackTrace();

			// 收集崩溃日志, 可以在后台上传给服务器,供开发人员分析
			try {
				PrintWriter err = new PrintWriter(
						Environment.getExternalStorageDirectory()
								+ "/err66.log");
				ex.printStackTrace(err);
				err.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 停止当前进程
			android.os.Process.killProcess(android.os.Process.myPid());
		}

	}
}
