package com.itheima.mobilesafe66.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.db.dao.AppLockDao;
import com.itheima.mobilesafe66.domain.AppInfo;
import com.itheima.mobilesafe66.engine.AppInfoProvider;

/**
 * 程序锁
 * 
 * @author Kevin
 * @date 2015-8-3
 */
public class AppLockActivity extends Activity implements OnClickListener {

	private Button btnUnlock;
	private Button btnLock;

	private LinearLayout llUnlock;
	private LinearLayout llLock;

	private ListView lvUnlock;
	private ListView lvLock;

	private ArrayList<AppInfo> mUnlockList;
	private ArrayList<AppInfo> mLockList;

	private AppLockAdapter mUnlockAdapter;
	private AppLockAdapter mLockAdapter;

	private TextView tvUnlock;
	private TextView tvLock;

	private AppLockDao mDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applock);

		mDao = AppLockDao.getInstance(this);

		btnUnlock = (Button) findViewById(R.id.btn_unlock);
		btnLock = (Button) findViewById(R.id.btn_lock);
		btnUnlock.setOnClickListener(this);
		btnLock.setOnClickListener(this);

		llUnlock = (LinearLayout) findViewById(R.id.ll_unlock);
		llLock = (LinearLayout) findViewById(R.id.ll_lock);

		lvUnlock = (ListView) findViewById(R.id.lv_unlock);
		lvLock = (ListView) findViewById(R.id.lv_lock);

		tvUnlock = (TextView) findViewById(R.id.tv_unlock);
		tvLock = (TextView) findViewById(R.id.tv_lock);

		initData();
	}

	private void initData() {
		new Thread() {
			@Override
			public void run() {
				ArrayList<AppInfo> list = AppInfoProvider
						.getIntalledApps(getApplicationContext());

				mLockList = new ArrayList<AppInfo>();
				mUnlockList = new ArrayList<AppInfo>();
				for (AppInfo appInfo : list) {
					if (mDao.find(appInfo.packageName)) {
						// 已加锁
						mLockList.add(appInfo);
					} else {
						// 未加锁
						mUnlockList.add(appInfo);
					}
				}

				runOnUiThread(new Runnable() {
					public void run() {
						// 未加锁设置数据
						mUnlockAdapter = new AppLockAdapter(false);
						lvUnlock.setAdapter(mUnlockAdapter);

						// 已加锁设置数据
						mLockAdapter = new AppLockAdapter(true);
						lvLock.setAdapter(mLockAdapter);
					};
				});
			}
		}.start();
	}

	/**
	 * 更新已加锁未加锁数量
	 */
	private void updateLockNum() {
		tvUnlock.setText("未加锁软件:" + mUnlockList.size() + "个");
		tvLock.setText("已加锁软件:" + mLockList.size() + "个");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_unlock:
			llUnlock.setVisibility(View.VISIBLE);
			llLock.setVisibility(View.GONE);

			btnUnlock.setBackgroundResource(R.drawable.tab_left_pressed);
			btnLock.setBackgroundResource(R.drawable.tab_right_default);
			break;
		case R.id.btn_lock:
			llUnlock.setVisibility(View.GONE);
			llLock.setVisibility(View.VISIBLE);

			btnUnlock.setBackgroundResource(R.drawable.tab_left_default);
			btnLock.setBackgroundResource(R.drawable.tab_right_pressed);
			break;

		default:
			break;
		}
	}

	class AppLockAdapter extends BaseAdapter {

		private boolean isLock;
		
		private TranslateAnimation animRight;
		private TranslateAnimation animLeft;

		public AppLockAdapter(boolean isLock) {
			this.isLock = isLock;
			
			// 右移动画
			animRight = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 1,
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 0);

			animRight.setDuration(500);
			
			// 左移动画
			animLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, -1,
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 0);

			animLeft.setDuration(500);
		}

		@Override
		public int getCount() {
			// 每次刷新都会走此方法, 在这里更新最新的加锁数量
			updateLockNum();

			if (isLock) {
				return mLockList.size();
			} else {
				return mUnlockList.size();
			}
		}

		@Override
		public AppInfo getItem(int position) {
			if (isLock) {
				return mLockList.get(position);
			} else {
				return mUnlockList.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.list_item_applock, null);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.ivIcon = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.ivLock = (ImageView) convertView
						.findViewById(R.id.iv_lock);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final View view = convertView;

			final AppInfo info = getItem(position);
			holder.tvName.setText(info.name);
			holder.ivIcon.setImageDrawable(info.icon);
			holder.ivLock.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!isLock) {
						animRight.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
								// 动画开始
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// 动画重复
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// 动画结束
								// 1. 数据库增加app
								// 2. 已加锁集合增加对象
								// 3. 未加锁集合减少对象
								// 4. 刷新listview
								mDao.add(info.packageName);
								mLockList.add(info);
								mUnlockList.remove(info);

								mUnlockAdapter.notifyDataSetChanged();
								mLockAdapter.notifyDataSetChanged();
							}
						});

						// 动画异步执行,不会阻塞
						view.startAnimation(animRight);
					} else {
						animLeft.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
								// 动画开始
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// 动画重复
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// 动画结束
								// 1. 从数据库删除
								// 2. 已加锁集合删除对象
								// 3. 未加锁集合增加对象
								// 4. 刷新listview
								mDao.delete(info.packageName);
								mLockList.remove(info);
								mUnlockList.add(info);

								mUnlockAdapter.notifyDataSetChanged();
								mLockAdapter.notifyDataSetChanged();
							}
						});

						// 动画异步执行,不会阻塞
						view.startAnimation(animLeft);
					}
				}
			});

			if (isLock) {
				holder.ivLock.setImageResource(R.drawable.unlock);
			} else {
				holder.ivLock.setImageResource(R.drawable.lock);
			}

			return convertView;
		}

	}

	static class ViewHolder {
		public TextView tvName;
		public ImageView ivIcon;
		public ImageView ivLock;
	}
}
