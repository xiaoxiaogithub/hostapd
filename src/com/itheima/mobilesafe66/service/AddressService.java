package com.itheima.mobilesafe66.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.db.dao.AddressDao;
import com.itheima.mobilesafe66.utils.PrefUtils;

/**
 * 归属地显示的服务
 * 
 * @author Kevin
 * 
 */
public class AddressService extends Service {

	private TelephonyManager mTM;
	private MyListener mListener;
	private InnerReceiver mReceiver;
	private WindowManager mWM;
	// private TextView mView;
	private View mView;

	private int startX;
	private int startY;

	private int mScreenWidth;
	private int mScreenHeight;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 监听来电
		mTM = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mListener = new MyListener();// 电话状态监听器
		// 监听来电状态
		mTM.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);

		// 监听去电
		// 动态注册广播, 可以在服务开启时注册,在服务销毁时注销
		mReceiver = new InnerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(mReceiver, filter);
	}

	class MyListener extends PhoneStateListener {

		// 电话状态发生变化后
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				// 电话铃响
				String address = AddressDao.getAddress(incomingNumber);// 获取归属地
				// Toast.makeText(getApplicationContext(), address,
				// Toast.LENGTH_LONG).show();
				showToast(address);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// 摘机状态, 正在通话

				break;
			case TelephonyManager.CALL_STATE_IDLE:
				// 电话空闲状态
				if (mWM != null && mView != null) {
					mWM.removeView(mView);// 从窗口移除布局
				}
				break;

			default:
				break;
			}

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消来电监听
		mTM.listen(mListener, PhoneStateListener.LISTEN_NONE);
		mListener = null;

		// 取消去电监听
		unregisterReceiver(mReceiver);
		mReceiver = null;
	}

	/**
	 * 监听去电 权限: <uses-permission
	 * android:name="android.permission.PROCESS_OUTGOING_CALLS"/>
	 */
	class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String number = getResultData();
			String address = AddressDao.getAddress(number);
			// Toast.makeText(getApplicationContext(), address,
			// Toast.LENGTH_LONG)
			// .show();
			showToast(address);
		}
	}

	/**
	 * 为了保证可触摸:
	 * 
	 * 1. params.flags, 删掉FLAG_NOT_TOUCHABLE 2. params.type更改为TYPE_PHONE 3. 加权限
	 * 
	 * 需要权限 <uses-permission
	 * android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
	 * 
	 * @param text
	 */
	private void showToast(String text) {
		// 窗口管理器, android系统最高层的界面,可以展示activity,通知栏
		mWM = (WindowManager) getSystemService(WINDOW_SERVICE);

		mScreenWidth = mWM.getDefaultDisplay().getWidth();// 屏幕宽度
		mScreenHeight = mWM.getDefaultDisplay().getHeight();// 屏幕高度

		// 初始化布局参数
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		// | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_PHONE;// 修改窗口类型为TYPE_PHONE,保证可以触摸
		params.setTitle("Toast");
		params.gravity = Gravity.LEFT + Gravity.TOP;// 将重心设定到左上方的位置,这样的话,坐标体系就以左上方为准,
													// 方便设定布局位置, 默认是Center

		// 初始化布局
		// mView = new TextView(this);
		// mView.setText(text);
		// mView.setTextColor(Color.RED);
		// mView.setTextSize(22);
		mView = View.inflate(this, R.layout.custom_toast, null);
		TextView tvAddress = (TextView) mView.findViewById(R.id.tv_address);

		// 取出保存的样式
		int style = PrefUtils.getInt("address_style", 0, this);
		// 背景图片id数组
		int[] bgIds = new int[] { R.drawable.call_locate_white,
				R.drawable.call_locate_orange, R.drawable.call_locate_blue,
				R.drawable.call_locate_gray, R.drawable.call_locate_green };
		// 更新背景图片
		tvAddress.setBackgroundResource(bgIds[style]);
		tvAddress.setText(text);

		// int x = params.x;//相对于默认重心的x偏移
		// 修改布局位置
		int lastX = PrefUtils.getInt("lastX", 0, this);
		int lastY = PrefUtils.getInt("lastY", 0, this);
		params.x = lastX;
		params.y = lastY;

		mView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// System.out.println("按下事件");
					// 记录起始点坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();

					break;
				case MotionEvent.ACTION_MOVE:
					// System.out.println("移动事件");
					// 获取移动后的坐标点
					int endX = (int) event.getRawX();
					int endY = (int) event.getRawY();

					// 计算移动偏移量
					int dx = endX - startX;
					int dy = endY - startY;

					// 根据偏移量更新位置
					params.x = params.x + dx;
					params.y = params.y + dy;

					// 防止布局x, y坐标超出屏幕边界
					if (params.x < 0) {
						params.x = 0;
					}
					// 防止布局x, y坐标超出屏幕边界
					if (params.x > mScreenWidth - mView.getWidth()) {
						params.x = mScreenWidth - mView.getWidth();
					}
					// 防止布局x, y坐标超出屏幕边界
					if (params.y < 0) {
						params.y = 0;
					}
					// 防止布局x, y坐标超出屏幕边界
					if (params.y > mScreenHeight - mView.getHeight() - 25) {// 减掉状态栏高度
						params.y = mScreenHeight - mView.getHeight() - 25;
					}

					System.out.println("x:" + params.x);
					System.out.println("y:" + params.y);

					// 更新窗口布局
					mWM.updateViewLayout(mView, params);

					// 重新初始化起始点坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					// System.out.println("抬起事件");
					// 保存当前位置
					PrefUtils
							.putInt("lastX", params.x, getApplicationContext());
					PrefUtils
							.putInt("lastY", params.y, getApplicationContext());
					break;

				default:
					break;
				}

				return true;// 返回true表示要消耗掉事件
			}
		});

		mWM.addView(mView, params);
	}

}
