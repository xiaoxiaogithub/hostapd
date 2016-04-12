package com.itheima.mobilesafe66.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.itheima.mobilesafe66.engine.ProcessInfoProvider;

/**
 * 锁屏清理
 * 
 * @author Kevin
 * 
 */
public class AutoKillService extends Service {

	private InnerScreenOffReceiver mReceiver;
	private Timer mTimer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 注册广播监听屏幕关闭
		mReceiver = new InnerScreenOffReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);

		// 定时清理
		mTimer = new Timer();// 初始化定时器
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println("定时清理啦!!!");
			}

		}, 0, 5000);// 每隔5秒执行一次
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销广播
		unregisterReceiver(mReceiver);
		mReceiver = null;

		// 停止定时器
		mTimer.cancel();
		mTimer = null;
	}

	class InnerScreenOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ProcessInfoProvider.killAll(context);
		}

	}
}
