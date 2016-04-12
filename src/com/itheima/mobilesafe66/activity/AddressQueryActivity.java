package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.db.dao.AddressDao;

public class AddressQueryActivity extends Activity {

	private EditText etNumber;
	private Button btnStart;
	private TextView tvResult;

	//我的代码2

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_address_query);

		etNumber = (EditText) findViewById(R.id.et_number);
		btnStart = (Button) findViewById(R.id.btn_start);
		tvResult = (TextView) findViewById(R.id.tv_result);

		btnStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String number = etNumber.getText().toString().trim();
				if (!TextUtils.isEmpty(number)) {
					String address = AddressDao.getAddress(number);
					tvResult.setText(address);
				} else {
					// ToastUtils.showToast(getApplicationContext(),
					// "输入内容不能为空!");
					Animation shake = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.shake);

					// 自定义插补器
					// shake.setInterpolator(new Interpolator() {
					//
					// @Override
					// public float getInterpolation(float x) {
					// float y = x;
					// return y;
					// }
					// });

					etNumber.startAnimation(shake);
					vibrator();
				}
			}
		});

		// 添加文本框内容变化监听器
		etNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				System.out.println("文本变化之前");
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				System.out.println("文本发生变化");
			}

			@Override
			public void afterTextChanged(Editable s) {
				System.out.println("文本变化之后");
				String address = AddressDao.getAddress(s.toString());
				tvResult.setText(address);
			}
		});
	}

	/**
	 * 振动 需要权限:android.permission.VIBRATE
	 */
	private void vibrator() {
		// 振动器
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		// vibrator.vibrate(2000);// 振动一定时间
		vibrator.vibrate(new long[] { 1000, 2000, 2000, 3000 }, 2);// 参1:振动模式,奇数位表示休息时间,偶数为表示振动时间
																	// 参2:-1表示不重复,
																	// 0表示从第0个位置开始重复
		// vibrator.cancel();//停止振动
	}

}
