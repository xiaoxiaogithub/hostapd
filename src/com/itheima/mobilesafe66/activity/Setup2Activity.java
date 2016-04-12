package com.itheima.mobilesafe66.activity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.utils.PrefUtils;
import com.itheima.mobilesafe66.utils.ToastUtils;
import com.itheima.mobilesafe66.view.SettingItemView;

/**
 * 设置向导2
 * 
 * @author Kevin
 * 
 */
public class Setup2Activity extends BaseSetupActivity {

	private SettingItemView sivBind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);

		sivBind = (SettingItemView) findViewById(R.id.siv_bind);

		String bindSim = PrefUtils.getString("bind_sim", null, this);
		if (TextUtils.isEmpty(bindSim)) {
			sivBind.setChecked(false);
		} else {
			sivBind.setChecked(true);
		}

		sivBind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (sivBind.isChecked()) {
					sivBind.setChecked(false);
					PrefUtils.remove("bind_sim", getApplicationContext());
				} else {
					sivBind.setChecked(true);
					// 初始化电话管理器
					TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
					String simSerialNumber = tm.getSimSerialNumber();// 获取sim卡序列号,需要权限:android.permission.READ_PHONE_STATE
					PrefUtils.putString("bind_sim", simSerialNumber,
							getApplicationContext());// 保存sim卡序列号
				}
			}
		});
	}

	/**
	 * 跳转上一页
	 */
	public void showPrevious() {
		startActivity(new Intent(this, Setup1Activity.class));
		finish();
		overridePendingTransition(R.anim.anim_previous_in,
				R.anim.anim_previous_out);
	}

	/**
	 * 跳转下一页
	 */
	public void showNext() {
		// 判断是否已经绑定sim卡,只有绑定,才能进行下一步操作
		String bindSim = PrefUtils.getString("bind_sim", null, this);
		if (TextUtils.isEmpty(bindSim)) {
			ToastUtils.showToast(this, "必须绑定sim卡!");
			return;
		}

		startActivity(new Intent(this, Setup3Activity.class));
		finish();
		// 两个activity之间切换的动画, 应该放在finish之后运行
		overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
	}

}
