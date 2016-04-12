package com.itheima.mobilesafe66.activity;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;

/**
 * 缓存清理
 * 
 * @author Kevin
 * @date 2015-8-4
 */
public class CleanCacheActivity extends Activity {

	private static final int STATE_UPDATE_STATUS = 1;// 更新扫描状态
	private static final int STATE_SCAN_FINISH = 2;// 扫描结束
	private static final int STATE_FIND_CACHE = 3;// 发现缓存

	private ProgressBar pbProgress;
	private TextView tvStatus;
	private LinearLayout llContainer;
	private PackageManager mPM;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case STATE_UPDATE_STATUS:
				String name = (String) msg.obj;
				tvStatus.setText("正在扫描:" + name);
				break;
			case STATE_FIND_CACHE:
				final CacheInfo info = (CacheInfo) msg.obj;

				View view = View.inflate(getApplicationContext(),
						R.layout.list_item_cacheinfo, null);
				TextView tvName = (TextView) view.findViewById(R.id.tv_name);
				ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
				TextView tvCacheSize = (TextView) view
						.findViewById(R.id.tv_cache_size);
				ImageView ivDelete = (ImageView) view
						.findViewById(R.id.iv_delete);

				tvName.setText(info.name);
				ivIcon.setImageDrawable(info.icon);
				tvCacheSize.setText(Formatter.formatFileSize(
						getApplicationContext(), info.cacheSize));
				ivDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 清理单个缓存
						// deleteApplicationCacheFiles, 只有系统应用才有此权限
						// 需要跳到系统页面清理缓存
						// Starting: Intent {
						// act=android.settings.APPLICATION_DETAILS_SETTINGS
						// dat=package:com.android.browser flg=0x10000000
						// cmp=com.android.settings/.applications.InstalledAppDetails
						// } from pid 200
						Intent intent = new Intent(
								"android.settings.APPLICATION_DETAILS_SETTINGS");
						intent.setData(Uri.parse("package:" + info.packageName));
						startActivity(intent);
					}
				});

				llContainer.addView(view, 0);
				break;

			case STATE_SCAN_FINISH:
				tvStatus.setText("扫描完毕");
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_cache);

		pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
		tvStatus = (TextView) findViewById(R.id.tv_status);
		llContainer = (LinearLayout) findViewById(R.id.ll_container);

		mPM = getPackageManager();
		new Thread() {
			public void run() {
				List<PackageInfo> installedPackages = mPM
						.getInstalledPackages(0);
				pbProgress.setMax(installedPackages.size());
				int progress = 0;
				for (PackageInfo packageInfo : installedPackages) {
					String packageName = packageInfo.packageName;
					// 权限:android.permission.GET_PACKAGE_SIZE
					try {
						Method method = mPM.getClass().getMethod(
								"getPackageSizeInfo", String.class,
								IPackageStatsObserver.class);
						method.invoke(mPM, packageName, new MyObserver());
					} catch (Exception e) {
						e.printStackTrace();
					}

					String name = packageInfo.applicationInfo.loadLabel(mPM)
							.toString();

					progress++;
					pbProgress.setProgress(progress);
					Message msg = Message.obtain();
					msg.what = STATE_UPDATE_STATUS;
					msg.obj = name;
					mHandler.sendMessage(msg);

					SystemClock.sleep(100);
				}

				mHandler.sendEmptyMessage(STATE_SCAN_FINISH);
			};
		}.start();
	}

	/**
	 * 清理全部缓存 权限:android.permission.CLEAR_APP_CACHE
	 * 
	 * @param view
	 */
	public void clearCache(View view) {
		// 立即清理
		// freeStorageAndNotify 向系统索要足够大的空间, 那么系统就会删除所有缓存文件来凑空间,从而间接达到清理缓存的目的
		// LRU least recentlly used 最近最少使用
		try {
			Method method = mPM.getClass().getMethod("freeStorageAndNotify",
					long.class, IPackageDataObserver.class);
			method.invoke(mPM, Long.MAX_VALUE, new IPackageDataObserver.Stub() {

				@Override
				public void onRemoveCompleted(String packageName,
						boolean succeeded) throws RemoteException {
					System.out.println("succeeded:" + succeeded);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class MyObserver extends IPackageStatsObserver.Stub {

		// 此方法在子线程运行
		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			long cacheSize = pStats.cacheSize;

			if (cacheSize > 0) {// 有些手机会默认给每个app预留至少12KB,无法清掉, cacheSize >
								// 12*1024
				// 有缓存
				CacheInfo info = new CacheInfo();
				info.packageName = pStats.packageName;
				info.cacheSize = cacheSize;

				// 根据包名获取应用信息
				ApplicationInfo applicationInfo;
				try {
					applicationInfo = mPM.getApplicationInfo(info.packageName,
							0);
					info.name = applicationInfo.loadLabel(mPM).toString();
					info.icon = applicationInfo.loadIcon(mPM);

					Message msg = Message.obtain();
					msg.obj = info;
					msg.what = STATE_FIND_CACHE;
					mHandler.sendMessage(msg);

				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

			}
		}
	}

	class CacheInfo {
		public String name;
		public String packageName;
		public Drawable icon;
		public long cacheSize;
	}
}
