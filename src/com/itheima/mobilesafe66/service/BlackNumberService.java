package com.itheima.mobilesafe66.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.itheima.mobilesafe66.db.dao.BlackNumberDao;

/**
 * 黑名单拦截服务
 * 
 * @author Kevin
 * 
 */
public class BlackNumberService extends Service {

	private InnerSmsReceiver mReceiver;
	private BlackNumberDao mDao;
	private TelephonyManager mTM;
	private MyListener mListener;
	private MyObserver mObserver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDao = BlackNumberDao.getInstance(this);

		// 拦截短信, 同等条件下,动态注册更优先获取广播
		mReceiver = new InnerSmsReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(Integer.MAX_VALUE);
		registerReceiver(mReceiver, filter);

		// 拦截电话
		mTM = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

		mListener = new MyListener();
		mTM.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	class MyListener extends PhoneStateListener {

		// 电话状态发生变化后
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				// 电话铃响, 1, 3
				int mode = mDao.findMode(incomingNumber);
				if (mode == 1 || mode == 3) {
					endCall();
					// 系统添加通话记录是异步操作的,速度稍微慢一点,在此处删除时,很有可能通话记录还没添加进来
					// 需要等系统将通话记录添加完成之后再删除
					// deleteCalllog(incomingNumber);
					mObserver = new MyObserver(new Handler(), incomingNumber);
					getContentResolver().registerContentObserver(
							Uri.parse("content://call_log/calls"), true,
							mObserver);
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
		// 注销短信监听
		unregisterReceiver(mReceiver);
		mReceiver = null;

		// 取消来电监听
		mTM.listen(mListener, PhoneStateListener.LISTEN_NONE);
		mListener = null;
	}

	/**
	 * 删除通话记录 权限: <uses-permission
	 * android:name="android.permission.WRITE_CONTACTS" />(2.3需要此权限)
	 * <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
	 */
	public void deleteCalllog(String number) {
		getContentResolver().delete(Uri.parse("content://call_log/calls"),
				"number=?", new String[] { number });
	}

	// 内容观察者
	class MyObserver extends ContentObserver {

		private String incomingNumber;

		public MyObserver(Handler handler, String incomingNumber) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}

		// 数据发生变化,就走此构造方法
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			System.out.println("通话记录发生变化!!!");
			deleteCalllog(incomingNumber);

			// 注销内容观察者
			getContentResolver().unregisterContentObserver(mObserver);
		}

	}

	/**
	 * 挂断电话 1. 通过debug找到ContextImpl 2. 找getSystemService方法 3. 找ServiceManager 4.
	 * ITelephony.aidl 5. 配置权限 <uses-permission
	 * android:name="android.permission.CALL_PHONE" />
	 * 
	 */
	public void endCall() {
		System.out.println("挂断电话!");
		// TelephonyManager, endCall()
		// IBinder b = ServiceManager.getService(TELEPHONY_SERVICE);
		// ITelephony service = ITelephony.Stub.asInterface(b);
		try {
			Class clazz = Class.forName("android.os.ServiceManager");
			Method method = clazz.getMethod("getService", String.class);
			IBinder b = (IBinder) method.invoke(null, TELEPHONY_SERVICE);// 静态方法不需要对象,参1是null
			ITelephony service = ITelephony.Stub.asInterface(b);
			service.endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class InnerSmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Object[] objs = (Object[]) intent.getExtras().get("pdus");

			for (Object obj : objs) {// 超过140字节,会分多条短信发送
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) obj);

				String originatingAddress = sms.getOriginatingAddress();
				String messageBody = sms.getMessageBody();

				System.out.println("短信号码:" + originatingAddress + ";短信内容:"
						+ messageBody);

				if (messageBody.contains("fapiao")) {// 根据短信内容智能拦截
					// laogong,nikanwodetofapiaobupiaoliangya? 分词
					// laogong, nikan,wode, tofa, piaobupiaoliang,ya
					// lucene分词检索框架
					abortBroadcast();
				}

				// 判断当前号码是否属于黑名单
				if (mDao.find(originatingAddress)) {
					// 短信2+全部3
					// 获取拦截模式
					int mode = mDao.findMode(originatingAddress);
					if (mode > 1) {
						abortBroadcast();
					}
				}
			}
		}
	}
}
