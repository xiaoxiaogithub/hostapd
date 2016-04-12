package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.utils.ToastUtils;

/**
 * 程序锁输入密码页面
 * 
 * 设置启动模式:
 *   <activity
            android:name="com.itheima.mobilesafe66.activity.EnterPwdActivity"
            android:launchMode="singleInstance" >
            
     任务栈只允许有一个activity, 解决手机卫士退到后台,被呼起的问题
     
   当此页面呼起时,不展示在最近运行的应用中
   
      <activity
            android:name="com.itheima.mobilesafe66.activity.EnterPwdActivity"
            android:launchMode="singleInstance" 
            android:excludeFromRecents="true"
            >
 * @author Kevin
 * @date 2015-8-3
 */
public class EnterPwdActivity extends Activity {

	private TextView tvName;
	private ImageView ivIcon;
	private EditText etPwd;
	private Button btnOK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enter_pwd);

		tvName = (TextView) findViewById(R.id.tv_name);
		ivIcon = (ImageView) findViewById(R.id.iv_icon);
		etPwd = (EditText) findViewById(R.id.et_pwd);
		btnOK = (Button) findViewById(R.id.btn_ok);

		Intent intent = getIntent();
		final String packageName = intent.getStringExtra("package");// 获取当前加锁应用包名

		// 根据包名获取app信息
		PackageManager pm = getPackageManager();
		try {
			ApplicationInfo applicationInfo = pm.getApplicationInfo(
					packageName, 0);
			String name = applicationInfo.loadLabel(pm).toString();
			Drawable icon = applicationInfo.loadIcon(pm);

			tvName.setText(name);
			ivIcon.setImageDrawable(icon);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String pwd = etPwd.getText().toString().trim();
				if (!TextUtils.isEmpty(pwd)) {
					if (pwd.equals("123")) {
						// 通知看门狗,跳过当前包名的验证
						Intent intent = new Intent();
						intent.setAction("com.itheima.mobilesafe66.SKIP_CHECK");
						intent.putExtra("package", packageName);//传递包名
						sendBroadcast(intent);

						finish();
					} else {
						ToastUtils.showToast(getApplicationContext(),
								"密码错误,密码是123哦!");
					}
				} else {
					ToastUtils.showToast(getApplicationContext(), "输入内容不能为空!");
				}
			}
		});
	}

	// 拦截物理返回键
	@Override
	public void onBackPressed() {
		// 跳到桌面
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);

		finish();
	}

	// 拦截物理键
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if(keyCode == KeyEvent.KEYCODE_BACK) {
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
}
