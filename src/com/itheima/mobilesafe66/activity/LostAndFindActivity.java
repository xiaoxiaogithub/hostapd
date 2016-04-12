package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.utils.PrefUtils;

/**
 * 手机防盗页面
 * 
 * @author Kevin
 * 
 */
public class LostAndFindActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 判断是否第一次进入
		boolean configed = PrefUtils.getBoolean("configed", false, this);
		if (!configed) {
			// 进入设置向导页面
			startActivity(new Intent(this, Setup1Activity.class));
			finish();
		} else {
			setContentView(R.layout.activity_lost_and_find);

			TextView tvPhone = (TextView) findViewById(R.id.tv_safe_phone);
			ImageView ivLock = (ImageView) findViewById(R.id.iv_lock);

			String phone = PrefUtils.getString("safe_phone", "", this);
			tvPhone.setText(phone);

			boolean protect = PrefUtils.getBoolean("protect", false, this);
			if (protect) {
				ivLock.setImageResource(R.drawable.lock);
			} else {
				ivLock.setImageResource(R.drawable.unlock);
			}
		}
	}

	/**
	 * 重新进入设置向导
	 * 
	 * @param view
	 */
	public void reSetup(View view) {
		startActivity(new Intent(this, Setup1Activity.class));
		finish();
	}

}
