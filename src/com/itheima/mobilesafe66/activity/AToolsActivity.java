package com.itheima.mobilesafe66.activity;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.utils.SmsUtils;
import com.itheima.mobilesafe66.utils.SmsUtils.SmsCallback;
import com.itheima.mobilesafe66.utils.ToastUtils;

public class AToolsActivity extends Activity {

	private ProgressBar pbProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atools);
		pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
		
		int i= 1/0;
	}

	/**
	 * 程序锁
	 * @param view
	 */
	public void appLock(View view) {
		startActivity(new Intent(this, AppLockActivity.class));
	}

	/**
	 * 常用号码查询
	 * 
	 * @param view
	 */
	public void commonNumberQuery(View view) {
		startActivity(new Intent(this, CommonNumberActivity.class));
	}

	/**
	 * 电话归属地
	 */
	public void addressQuery(View view) {
		startActivity(new Intent(this, AddressQueryActivity.class));
	}

	/**
	 * 短信备份
	 * 
	 * @param view
	 */
	public void smsBackup(View view) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("正在备份短信...");
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 水平方向进度条,可以展示进度
			dialog.show();

			new Thread() {
				public void run() {
					File output = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/sms66.xml");
					SmsUtils.smsBackup(getApplicationContext(), output,
							new SmsCallback() {

								@Override
								public void preSmsBackup(int count) {
									dialog.setMax(count);
									pbProgress.setMax(count);
								}

								@Override
								public void onSmsBackup(int progress) {
									dialog.setProgress(progress);
									pbProgress.setProgress(progress);
								}
							});

					dialog.dismiss();
				};
			}.start();
		} else {
			ToastUtils.showToast(this, "sdcard不存在!");
		}
	}

}
