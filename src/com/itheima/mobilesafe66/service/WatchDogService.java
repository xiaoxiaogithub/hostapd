package com.itheima.mobilesafe66.service;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import com.itheima.mobilesafe66.activity.EnterPwdActivity;
import com.itheima.mobilesafe66.db.dao.AppLockDao;

/**
 * 看门狗服务
 * 
 * @author Kevin
 * @date 2015-8-3
 */
public class WatchDogService extends Service {

	private ActivityManager mAM;

	// 此标记控制线程运行
	private boolean isRunning = true;

	private AppLockDao mDao;

	private MyReceiver mReceiver;

	private String mSkipPackage;// 要跳过验证的包名

	private ArrayList<String> mLockList;

	private MyContentObserver mObserver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mAM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mDao = AppLockDao.getInstance(this);

		mLockList = mDao.findAll();// 查询所有已加锁应用集合

		new Thread() {
			public void run() {
				while (isRunning) {
					// 获取当前屏幕展示的页面
					// 需要权限:android.permission.GET_TASKS
					List<RunningTaskInfo> runningTasks = mAM.getRunningTasks(1);// 获取当前运行的任务栈,
																				// 返回一条最新的任务栈
					String packageName = runningTasks.get(0).topActivity
							.getPackageName();// 获取栈顶activity所在的包名
					// System.out.println(packageName);
					// if (mDao.find(packageName)
					if (mLockList.contains(packageName)// 直接从内存查,避免频繁操作数据库
							&& !packageName.equals(mSkipPackage)) {// 如果不是需要跳过验证的包,
																	// 才跳输入密码页面
						// 跳转输入密码页面
						Intent intent = new Intent(getApplicationContext(),
								EnterPwdActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("package", packageName);// 将包名传递给输入密码页面
						startActivity(intent);
					}

					// Thread.sleep(100);
					// 看门狗休息一下
					SystemClock.sleep(100);// 这个方法不用try catch
				}
			};
		}.start();

		// 注册跳过验证的广播接收者
		mReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.itheima.mobilesafe66.SKIP_CHECK");
		registerReceiver(mReceiver, filter);

		// 监听程序锁数据库变化
		mObserver = new MyContentObserver(null);
		getContentResolver().registerContentObserver(
				Uri.parse("content://com.itheima.mobilesafe66/change"), true,
				mObserver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;// 停止线程

		unregisterReceiver(mReceiver);
		mReceiver = null;

		// 注销数据库监听
		getContentResolver().unregisterContentObserver(mObserver);
		mObserver = null;
	}

	class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mSkipPackage = intent.getStringExtra("package");
		}
	}

	class MyContentObserver extends ContentObserver {

		public MyContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			// 重新拉取数据库最新数据
			mLockList = mDao.findAll();// 查询所有已加锁应用集合
		}
	}

}
