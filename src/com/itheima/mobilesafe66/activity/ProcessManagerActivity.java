package com.itheima.mobilesafe66.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.activity.AppManagerActivity.HeaderHolder;
import com.itheima.mobilesafe66.domain.ProcessInfo;
import com.itheima.mobilesafe66.engine.ProcessInfoProvider;
import com.itheima.mobilesafe66.utils.PrefUtils;
import com.itheima.mobilesafe66.utils.ToastUtils;

/**
 * 进程管理
 * 
 * item选中效果处理: 1. 禁用checkbox原生点击效果 2. ProcessInfo新增属性isChecked,标记是否选中 3.
 * getView中根据isChecked来更新checkbox 4. item点击事件, 将ischecked置为相反值, 同时更新checkbox
 * 
 * @author Kevin
 * 
 */
public class ProcessManagerActivity extends Activity {

	private TextView tvRunningNum;
	private TextView tvMemoInfo;

	private int mRunningProcessNum;
	private long mAvailMemory;
	private long mTotalMemory;

	private ListView lvList;
	private TextView tvHeader;
	private LinearLayout llLoading;

	private ArrayList<ProcessInfo> mUserList;// 所有已安装用户进程的集合
	private ArrayList<ProcessInfo> mSystemList;// 所有已安装系统进程的集合

	private ProcessInfoAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process_manager);

		tvRunningNum = (TextView) findViewById(R.id.tv_running_num);
		tvMemoInfo = (TextView) findViewById(R.id.tv_memo_info);
		lvList = (ListView) findViewById(R.id.lv_list);
		tvHeader = (TextView) findViewById(R.id.tv_header);
		llLoading = (LinearLayout) findViewById(R.id.ll_loading);

		mRunningProcessNum = ProcessInfoProvider.getRunningProcessNum(this);
		// tvRunningNum.setText("运行中的进程:" + mRunningProcessNum + "个");
		tvRunningNum.setText(String.format("运行中的进程:%d个", mRunningProcessNum));

		mAvailMemory = ProcessInfoProvider.getAvailMemory(this);
		mTotalMemory = ProcessInfoProvider.getTotalMemory(this);
		tvMemoInfo.setText(String.format("剩余/总内存:%s/%s",
				Formatter.formatFileSize(this, mAvailMemory),
				Formatter.formatFileSize(this, mTotalMemory)));

		lvList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mUserList != null && mSystemList != null) {
					if (firstVisibleItem <= mUserList.size()) {
						tvHeader.setText("用户进程(" + mUserList.size() + ")");
					} else {
						tvHeader.setText("系统进程(" + mSystemList.size() + ")");
					}
				}
			}
		});

		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ProcessInfo info = mAdapter.getItem(position);
				if (info != null) {
					if (info.packageName.equals(getPackageName())) {
						// 过滤掉手机卫士
						return;
					}

					info.isChecked = !info.isChecked;
					// mAdapter.notifyDataSetChanged();//全局刷新,性能不太好
					// 局部更新checkbox
					CheckBox cbCheck = (CheckBox) view
							.findViewById(R.id.cb_check);
					cbCheck.setChecked(info.isChecked);
				}
			}
		});

		initData();
	}

	private void initData() {
		llLoading.setVisibility(View.VISIBLE);
		new Thread() {
			@Override
			public void run() {
				ArrayList<ProcessInfo> list = ProcessInfoProvider
						.getRunnningProcesses(getApplicationContext());

				mUserList = new ArrayList<ProcessInfo>();
				mSystemList = new ArrayList<ProcessInfo>();
				for (ProcessInfo processInfo : list) {
					if (processInfo.isUser) {
						mUserList.add(processInfo);
					} else {
						mSystemList.add(processInfo);
					}
				}

				runOnUiThread(new Runnable() {

					public void run() {
						mAdapter = new ProcessInfoAdapter();
						lvList.setAdapter(mAdapter);
						llLoading.setVisibility(View.GONE);
					};
				});
			}
		}.start();
	}

	class ProcessInfoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// return mList.size();
			boolean showSystem = PrefUtils.getBoolean("show_system", true,
					getApplicationContext());
			if (showSystem) {// 显示系统进程
				return mUserList.size() + mSystemList.size() + 2;// 增加两个标题栏
			} else {
				return mUserList.size() + 1;
			}
		}

		@Override
		public ProcessInfo getItem(int position) {
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
					hHolder.tvHeader.setText("用户进程(" + mUserList.size() + ")");
				} else {
					hHolder.tvHeader
							.setText("系统进程(" + mSystemList.size() + ")");
				}
				break;
			case 1:// 普通类型
					// View view = null;
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = View.inflate(getApplicationContext(),
							R.layout.list_item_processinfo, null);

					holder = new ViewHolder();
					holder.tvName = (TextView) convertView
							.findViewById(R.id.tv_name);
					holder.tvMemory = (TextView) convertView
							.findViewById(R.id.tv_memory);
					holder.ivIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holder.cbCheck = (CheckBox) convertView
							.findViewById(R.id.cb_check);

					convertView.setTag(holder);
				} else {
					// view = convertView;
					holder = (ViewHolder) convertView.getTag();
				}

				ProcessInfo info = getItem(position);
				holder.tvName.setText(info.name);
				holder.ivIcon.setImageDrawable(info.icon);
				holder.tvMemory.setText(Formatter.formatFileSize(
						getApplicationContext(), info.memory));

				if (info.packageName.equals(getPackageName())) {
					// 当前应用, 不显示checkbox,过滤掉手机卫士
					holder.cbCheck.setVisibility(View.INVISIBLE);
				} else {
					holder.cbCheck.setVisibility(View.VISIBLE);
					holder.cbCheck.setChecked(info.isChecked);
				}
				break;
			}

			return convertView;
		}
	}

	static class ViewHolder {
		public TextView tvName;
		public TextView tvMemory;
		public ImageView ivIcon;
		public CheckBox cbCheck;
	}

	// 全选
	public void selectAll(View view) {
		for (ProcessInfo info : mUserList) {
			if (info.packageName.equals(getPackageName())) {
				// 过滤掉手机卫士
				continue;
			}

			info.isChecked = true;
		}

		boolean showSystem = PrefUtils.getBoolean("show_system", true, this);
		if (showSystem) {// 如果不展示系统应用,就不用清除系统进程
			for (ProcessInfo info : mSystemList) {
				info.isChecked = true;
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	// 反选
	public void reverseSelect(View view) {
		for (ProcessInfo info : mUserList) {
			if (info.packageName.equals(getPackageName())) {
				// 过滤掉手机卫士
				continue;
			}

			info.isChecked = !info.isChecked;
		}

		boolean showSystem = PrefUtils.getBoolean("show_system", true, this);
		if (showSystem) {// 如果不展示系统应用,就不用清除系统进程
			for (ProcessInfo info : mSystemList) {
				info.isChecked = !info.isChecked;
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	// 一键清理
	// 需要权限: <uses-permission
	// android:name="android.permission.KILL_BACKGROUND_PROCESSES" />
	public void killAll(View view) {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// java.util.ConcurrentModificationException,并发修改异常,遍历集合过程中,修改集合元素个数
		// foreach会出现此问题
		ArrayList<ProcessInfo> killedList = new ArrayList<ProcessInfo>();// 被清理的进程集合
		for (ProcessInfo info : mUserList) {
			if (info.isChecked) {
				am.killBackgroundProcesses(info.packageName);
				// mUserList.remove(info);
				killedList.add(info);
			}
		}

		boolean showSystem = PrefUtils.getBoolean("show_system", true, this);
		if (showSystem) {// 如果不展示系统应用,就不用清除系统进程
			for (ProcessInfo info : mSystemList) {
				if (info.isChecked) {
					am.killBackgroundProcesses(info.packageName);
					// mSystemList.remove(info);
					killedList.add(info);
				}
			}
		}

		long savedMemory = 0;
		for (ProcessInfo processInfo : killedList) {
			if (processInfo.isUser) {
				mUserList.remove(processInfo);
			} else {
				mSystemList.remove(processInfo);
			}

			savedMemory += processInfo.memory;
		}

		mAdapter.notifyDataSetChanged();

		ToastUtils
				.showToast(this, String.format("帮您杀死了%d个进程,共节省%s空间!",
						killedList.size(),
						Formatter.formatFileSize(this, savedMemory)));

		// 更新文本信息
		mRunningProcessNum -= killedList.size();
		mAvailMemory += savedMemory;
		tvRunningNum.setText(String.format("运行中的进程:%d个", mRunningProcessNum));
		tvMemoInfo.setText(String.format("剩余/总内存:%s/%s",
				Formatter.formatFileSize(this, mAvailMemory),
				Formatter.formatFileSize(this, mTotalMemory)));

	}

	// 设置
	public void setting(View view) {
		startActivityForResult(new Intent(this, ProcessSettingActivity.class),
				0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 从设置页面返回后的回调
		mAdapter.notifyDataSetChanged();// 重新会走一次getCount方法,根据设置信息返回相应item数量
	}

}
