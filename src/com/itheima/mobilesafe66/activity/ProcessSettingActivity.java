package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.service.AutoKillService;
import com.itheima.mobilesafe66.utils.PrefUtils;
import com.itheima.mobilesafe66.utils.ServiceStatusUtils;

/**
 * 进程管理设置
 * 
 * @author Kevin
 * 
 */
public class ProcessSettingActivity extends Activity {

	private CheckBox cbShowSystem;
	private CheckBox cbAutoKill;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process_setting);
		cbShowSystem = (CheckBox) findViewById(R.id.cb_show_system);
		cbAutoKill = (CheckBox) findViewById(R.id.cb_auto_kill);

		boolean showSystem = PrefUtils.getBoolean("show_system", true, this);
		if (showSystem) {
			cbShowSystem.setChecked(true);
			cbShowSystem.setText("显示系统进程");
		} else {
			cbShowSystem.setChecked(false);
			cbShowSystem.setText("不显示系统进程");
		}

		cbShowSystem.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					cbShowSystem.setText("显示系统进程");
					PrefUtils.putBoolean("show_system", true,
							getApplicationContext());
				} else {
					cbShowSystem.setText("不显示系统进程");
					PrefUtils.putBoolean("show_system", false,
							getApplicationContext());
				}
			}
		});

		boolean serviceRunning = ServiceStatusUtils.isServiceRunning(
				"com.itheima.mobilesafe66.service.AutoKillService", this);
		if (serviceRunning) {
			cbAutoKill.setChecked(true);
			cbAutoKill.setText("锁屏清理已开启");
		} else {
			cbAutoKill.setChecked(false);
			cbAutoKill.setText("锁屏清理已关闭");
		}

		cbAutoKill.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Intent service = new Intent(getApplicationContext(),
						AutoKillService.class);
				if (isChecked) {
					cbAutoKill.setText("锁屏清理已开启");
					startService(service);
				} else {
					cbAutoKill.setText("锁屏清理已关闭");
					stopService(service);
				}
			}
		});
	}
}
