package com.itheima.mobilesafe66.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.domain.AppInfo;
import com.itheima.mobilesafe66.engine.AppInfoProvider;
import com.itheima.mobilesafe66.utils.ToastUtils;

/**
 * 软件管理
 * 
 * 手机apk安装流程: 1. 将apk拷贝到/data/app(用户) 2. 在/data/system中注册,
 * packages.list(系统用户应用,包名,项目路径), packages.xml(注册每个应用的权限)
 * 
 * 系统apk目录: /system/app(厂商内置), 不允许安装
 * 
 * 安装位置: android:installLocation="internalOnly"//只允许安装在手机内存(默认);preferExternal(
 * 优先sdcard);auto(优先手机内存,其次sdcard)
 * 
 * @author Kevin
 * 
 */
public class AppManagerActivity extends Activity implements OnClickListener {

	private ArrayList<AppInfo> mList;// 所有已安装应用的集合
	private ArrayList<AppInfo> mUserList;// 所有已安装用户应用的集合
	private ArrayList<AppInfo> mSystemList;// 所有已安装系统应用的集合

	private ListView lvList;
	private AppInfoAdapter mAdapter;
	private LinearLayout llLoading;
	private TextView tvHeader;
	private PopupWindow mPopupWindow;
	private View mPopupView;
	private AnimationSet mPopupAnimSet;

	private AppInfo mCurrentAppInfo;// 当前被选择的对象

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);

		String sdcardSpace = getAvailSpace(Environment
				.getExternalStorageDirectory().getAbsolutePath());
		String romSpace = getAvailSpace(Environment.getDataDirectory()
				.getAbsolutePath());// /data目录的可用空间

		TextView tvSdcard = (TextView) findViewById(R.id.tv_sdcard_avail);
		TextView tvRom = (TextView) findViewById(R.id.tv_rom_avail);

		tvSdcard.setText("sdcard可用:" + sdcardSpace);
		tvRom.setText("内部存储可用:" + romSpace);

		lvList = (ListView) findViewById(R.id.lv_list);
		llLoading = (LinearLayout) findViewById(R.id.ll_loading);
		tvHeader = (TextView) findViewById(R.id.tv_header);

		lvList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mUserList != null && mSystemList != null) {
					if (firstVisibleItem <= mUserList.size()) {
						tvHeader.setText("用户应用(" + mUserList.size() + ")");
					} else {
						tvHeader.setText("系统应用(" + mSystemList.size() + ")");
					}
				}
			}
		});

		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppInfo info = mAdapter.getItem(position);
				if (info != null) {// 只有普通布局才可以展示弹窗
					mCurrentAppInfo = info;
					showPopupWindow(view);
				}
			}
		});

		initData();
	}

	/**
	 * 展示小弹窗
	 */
	protected void showPopupWindow(View view) {
		if (mPopupWindow == null) {
			mPopupView = View.inflate(this, R.layout.popup_item_appinfo, null);
			mPopupWindow = new PopupWindow(mPopupView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable());// 必须设置背景,这样点击返回键或者空白处才可以消失

			TextView tvUninstall = (TextView) mPopupView
					.findViewById(R.id.tv_uninstall);
			TextView tvLaunch = (TextView) mPopupView
					.findViewById(R.id.tv_launch);
			TextView tvShare = (TextView) mPopupView
					.findViewById(R.id.tv_share);
			tvUninstall.setOnClickListener(this);
			tvLaunch.setOnClickListener(this);
			tvShare.setOnClickListener(this);

			// 弹窗动画效果
			// 渐变动画
			AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
			animAlpha.setDuration(500);

			// 缩放动画
			ScaleAnimation animScale = new ScaleAnimation(0, 1, 0, 1,
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
					0.5f);
			animScale.setDuration(500);

			mPopupAnimSet = new AnimationSet(true);
			mPopupAnimSet.addAnimation(animAlpha);
			mPopupAnimSet.addAnimation(animScale);
		}

		mPopupWindow.showAsDropDown(view, 50, -view.getHeight());
		mPopupView.startAnimation(mPopupAnimSet);
	}

	private void initData() {
		llLoading.setVisibility(View.VISIBLE);
		new Thread() {
			@Override
			public void run() {
				mList = AppInfoProvider
						.getIntalledApps(getApplicationContext());

				// 区分用户和系统应用,分别放在两个集合中
				mUserList = new ArrayList<AppInfo>();
				mSystemList = new ArrayList<AppInfo>();
				for (AppInfo info : mList) {
					if (info.isUser) {
						mUserList.add(info);
					} else {
						mSystemList.add(info);
					}
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mAdapter = new AppInfoAdapter();
						lvList.setAdapter(mAdapter);
						llLoading.setVisibility(View.GONE);
					}
				});
			}
		}.start();
	}

	/**
	 * 获取可用空间
	 */
	private String getAvailSpace(String path) {
		StatFs stat = new StatFs(path);

		long availableBlocks = stat.getAvailableBlocks();// 获取可用存储块数量
		long blockSize = stat.getBlockSize();// 每个存储块的大小

		// 可用存储空间
		long availSize = availableBlocks * blockSize;
		// Integer.MAX_VALUE 可以表示2G大小, 2G太少, 需要用Long

		return Formatter.formatFileSize(this, availSize);// 将字节转为带有相应单位(MB,
															// G)的字符串
	}

	class AppInfoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// return mList.size();
			return mUserList.size() + mSystemList.size() + 2;// 增加两个标题栏
		}

		@Override
		public AppInfo getItem(int position) {
			if (position == 0 || position == mUserList.size() + 1) {
				// 碰到标题栏了
				return null;
			}

			if (position < mUserList.size() + 1) {
				return mUserList.get(position - 1);// 减掉1个标题栏
			} else {
				return mSystemList.get(position - mUserList.size() - 2);// 减掉两个标题栏
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// 表示listview展示的布局种类数量, 只有重写此方法,系统才会缓存相应个数的convertView
		@Override
		public int getViewTypeCount() {
			return 2;
		}

		// 根据当前位置返回不同布局类型
		// 注意: 类型必须从0开始计数
		@Override
		public int getItemViewType(int position) {
			if (position == 0 || position == mUserList.size() + 1) {
				// 碰到标题栏了
				return 0;// 标题栏类型
			} else {
				return 1;// 普通类型
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 判断当前布局类型, 系统会根据当前布局类型,返回响应类型的convertView对象
			int type = getItemViewType(position);
			switch (type) {
			case 0:// 标题类型
				HeaderHolder hHolder = null;
				if (convertView == null) {
					convertView = View.inflate(getApplicationContext(),
							R.layout.list_item_header, null);
					hHolder = new HeaderHolder();
					hHolder.tvHeader = (TextView) convertView
							.findViewById(R.id.tv_header);
					convertView.setTag(hHolder);
				} else {
					hHolder = (HeaderHolder) convertView.getTag();
				}

				if (position == 0) {
					hHolder.tvHeader.setText("用户应用(" + mUserList.size() + ")");
				} else {
					hHolder.tvHeader
							.setText("系统应用(" + mSystemList.size() + ")");
				}
				break;
			case 1:// 普通类型
					// View view = null;
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = View.inflate(getApplicationContext(),
							R.layout.list_item_appinfo, null);

					holder = new ViewHolder();
					holder.tvName = (TextView) convertView
							.findViewById(R.id.tv_name);
					holder.tvLocation = (TextView) convertView
							.findViewById(R.id.tv_location);
					holder.ivIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);

					convertView.setTag(holder);
				} else {
					// view = convertView;
					holder = (ViewHolder) convertView.getTag();
				}

				AppInfo info = getItem(position);
				holder.tvName.setText(info.name);
				holder.ivIcon.setImageDrawable(info.icon);

				if (info.isRom) {
					holder.tvLocation.setText("手机内存");
				} else {
					holder.tvLocation.setText("外置存储卡");
				}
				break;
			}

			return convertView;
		}
	}

	static class ViewHolder {
		public TextView tvName;
		public TextView tvLocation;
		public ImageView ivIcon;
	}

	static class HeaderHolder {
		public TextView tvHeader;
	}

	@Override
	public void onClick(View v) {

		mPopupWindow.dismiss();// 弹窗消失

		switch (v.getId()) {
		case R.id.tv_uninstall:
			System.out.println("卸载:" + mCurrentAppInfo.name);
			uninstall();
			break;
		case R.id.tv_launch:
			System.out.println("启动");
			launch();
			break;
		case R.id.tv_share:
			System.out.println("分享");
			share();
			break;

		default:
			break;
		}
	}

	/**
	 * 分享 - 调用系统所有可以分享的app列表,选择app进行分享
	 */
	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");// 分享纯文本
		intent.putExtra(Intent.EXTRA_TEXT,
				"分享给你一个很好的应用哦,下载地址:https://play.google.com/store/apps/details?id="
						+ mCurrentAppInfo.packageName);
		startActivity(intent);
	}

	/**
	 * 启动
	 */
	private void launch() {
		PackageManager pm = getPackageManager();
		Intent intent = pm
				.getLaunchIntentForPackage(mCurrentAppInfo.packageName);// 启动页面的Intent
		if (intent != null) {
			startActivity(intent);
		} else {
			ToastUtils.showToast(this, "找不到启动页面!");
		}
	}

	/**
	 * 卸载
	 */
	private void uninstall() {
		if (mCurrentAppInfo.isUser) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:" + mCurrentAppInfo.packageName));
			startActivityForResult(intent, 0);
		} else {
			ToastUtils.showToast(this, "需要Root权限才可以卸载哦!");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 卸载完成后重新加载数据
		initData();
	}
}
