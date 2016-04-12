package com.itheima.mobilesafe66.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.db.dao.AntiVirusDao;
import com.itheima.mobilesafe66.utils.MD5Utils;
import com.itheima.mobilesafe66.utils.ToastUtils;

/**
 * 手机杀毒
 *  <activity
 * android:name="com.itheima.mobilesafe66.activity.AntiVirusActivity"
 * android:screenOrientation="portrait" >//强制竖屏 </activity>
 * 
 *  <activity
     android:name="com.itheima.mobilesafe66.activity.AntiVirusActivity"
     android:configChanges="orientation|screenSize|keyboardHidden" >//可以横屏,但不会走oncreate
 * 
 * @author Kevin
 * @date 2015-8-4
 */
public class AntiVirusActivity extends Activity {

	private static final int STATE_UPDATE_STATUS = 1;// 更新扫描状态
	private static final int STATE_SCAN_FINISH = 2;// 扫描结束

	private ImageView ivScanning;
	private TextView tvStatus;
	private ProgressBar pbProgress;
	private LinearLayout llContainer;

	// 病毒集合
	private ArrayList<ScanInfo> mVirusList = new ArrayList<AntiVirusActivity.ScanInfo>();

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case STATE_UPDATE_STATUS:
				ScanInfo info = (ScanInfo) msg.obj;
				tvStatus.setText("正在扫描:" + info.name);

				// 动态给容器llContainer添加TextView
				TextView view = new TextView(getApplicationContext());

				// 判断是否是病毒
				if (info.isVirus) {
					view.setText("发现病毒:" + info.name);
					view.setTextColor(Color.RED);
				} else {
					view.setText("扫描安全:" + info.name);
					view.setTextColor(Color.BLACK);
				}

				// llContainer.addView(view);
				llContainer.addView(view, 0);// 将view添加在第一个位置
				break;
			case STATE_SCAN_FINISH:
				tvStatus.setText("扫描完毕");
				ivScanning.clearAnimation();// 停止当前动画

				if (!mVirusList.isEmpty()) {
					showAlertDialog();
				} else {
					ToastUtils.showToast(getApplicationContext(),
							"您的手机很安全,请放心使用!");
				}

				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_virus);

		ivScanning = (ImageView) findViewById(R.id.iv_scanning);
		tvStatus = (TextView) findViewById(R.id.tv_status);
		pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
		llContainer = (LinearLayout) findViewById(R.id.ll_container);

		RotateAnimation anim = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setDuration(2000);
		anim.setInterpolator(new LinearInterpolator());// 匀速运动
		anim.setRepeatCount(Animation.INFINITE);// 无限循环
		ivScanning.startAnimation(anim);

		new Thread() {
			public void run() {
				// for (int i = 0; i <= 100; i++) {
				// pbProgress.setProgress(i);
				// SystemClock.sleep(100);
				// }

				SystemClock.sleep(2000);// 展示正在初始化8核引擎的文字

				PackageManager pm = getPackageManager();
				// 获取已安装app, 某些app卸载之后,还残留data/data目录数据,也要加载出来
				List<PackageInfo> installedPackages = pm
						.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

				pbProgress.setMax(installedPackages.size());
				int progress = 0;
				Random random = new Random();
				for (PackageInfo packageInfo : installedPackages) {
					ScanInfo info = new ScanInfo();

					String packageName = packageInfo.packageName;
					String name = packageInfo.applicationInfo.loadLabel(pm)
							.toString();

					info.packageName = packageName;
					info.name = name;

					// 计算当前apk的md5
					String apkPath = packageInfo.applicationInfo.sourceDir;// 获取应用apk文件的地址
					String md5 = MD5Utils.encodeFile(apkPath);

					// 判断当前apk是否是病毒
					if (AntiVirusDao.isVirus(md5)) {
						System.out.println("发现病毒!");
						info.isVirus = true;

						mVirusList.add(info);
					} else {
						System.out.println("扫描安全!");
						info.isVirus = false;
					}

					progress++;
					pbProgress.setProgress(progress);
					// 更新扫描状态
					Message msg = Message.obtain();
					msg.what = STATE_UPDATE_STATUS;
					msg.obj = info;
					mHandler.sendMessage(msg);

					SystemClock.sleep(50 + random.nextInt(50));// 休息50-100随机时间
				}

				// 扫描完毕
				mHandler.sendEmptyMessage(STATE_SCAN_FINISH);
			};
		}.start();
	}

	/**
	 * 发现病毒的警告弹窗
	 */
	protected void showAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("严重警告!");
		builder.setMessage("发现" + mVirusList.size() + "个病毒, 建议立即处理!!!");
		builder.setCancelable(false);
		builder.setPositiveButton("立即处理",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 卸载病毒
						for (ScanInfo info : mVirusList) {
							Intent intent = new Intent(Intent.ACTION_DELETE);
							intent.setData(Uri.parse("package:"
									+ info.packageName));
							startActivity(intent);
						}
					}
				});

		builder.setNegativeButton("以后再说", null);
		builder.show();
	}

	class ScanInfo {
		public boolean isVirus;
		public String name;
		public String packageName;
	}
}
