package com.itheima.mobilesafe66.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.db.dao.BlackNumberDao;
import com.itheima.mobilesafe66.domain.BlackNumberInfo;
import com.itheima.mobilesafe66.utils.ToastUtils;

/**
 * 黑名单管理 开发流程: 1. 布局文件 2. 封装黑名单数据库 3. listview填充数据 4. listview优化 5. 在子线程加载数据 6.
 * 异步加载黑名单数据 7. 分页加载(1.使用limit语句查询一页数据 2. 展示第一页数据 3. 监听滑动到底部的事件 4.加载下一页数据,
 * 5.判断是否到最后一页) 8.添加黑名单 9. 删除黑名单
 * 
 * @author Kevin
 * 
 */
public class BlackNumberActivity extends Activity {

	private ListView lvList;
	private BlackNumberDao mDao;
	private ArrayList<BlackNumberInfo> mList;
	private BlackNumberAdapter mAdapter;
	private ProgressBar pbLoading;

	private int mIndex;// 记录分页查询的位置

	private boolean isLoading;// 标记当前是否正在加载
	//private int mTotalCount;// 黑名单总个数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_black_number);
		mDao = BlackNumberDao.getInstance(this);

		//mTotalCount = mDao.getTotalCount();

		lvList = (ListView) findViewById(R.id.lv_black_number);
		pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

		// listview滑动监听
		lvList.setOnScrollListener(new OnScrollListener() {
			// 滑动状态发生变化
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {// 滑动空闲
					// 判断是否到底部
					// 判断当前屏幕展示的最后一个条目是否是集合中最后一个元素,如果是,就到底部了
					int lastVisiblePosition = lvList.getLastVisiblePosition();
					if (lastVisiblePosition >= mList.size() - 1 && !isLoading) {
						System.out.println("到底了...");
						// 判断是否到最后一页
						// 获取数据库数据的总个数, 和当前集合中的个数进行对比
						int totalCount = mDao.getTotalCount();
						if (mList.size() < totalCount) {
							initData();
						} else {
							ToastUtils.showToast(getApplicationContext(),
									"没有更多数据了");
						}
					}
				}
			}

			// 滑动过程中的回调
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		initData();
	}

	private void initData() {
		isLoading = true;// 表示正在加载

		// 显示进度条
		pbLoading.setVisibility(View.VISIBLE);
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);// 模拟耗时操作
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// mList = mDao.findAll();
				if (mList == null) {// 第一页数据
					mList = mDao.findPart(mIndex);// 查询一页数据
				} else {
					// 在原有集合上追加20条数据
					ArrayList<BlackNumberInfo> partList = mDao.findPart(mIndex);// 查询下一页数据
					// 1,2,3,4,5 , + 6,7,8,9
					mList.addAll(partList);
				}

				// 运行在主线程
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mAdapter == null) {// 第一页数据,才初始化adapter
							mAdapter = new BlackNumberAdapter();
							lvList.setAdapter(mAdapter);// 给listview设置数据,数据从第0个开始展示
						} else {
							// 刷新listview
							mAdapter.notifyDataSetChanged();// 基于当前数据,进行刷新操作,当前在哪个item,就展现在哪个item
						}

						// mIndex += 20;
						// 20, 40, 60, 80
						mIndex = mList.size();// 更新分页位置

						// 隐藏进度条
						pbLoading.setVisibility(View.GONE);
						isLoading = false;// 加载结束
					}
				});
			}
		}.start();
	}

	class BlackNumberAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public BlackNumberInfo getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// listview重用
		// 1. 重用convertView, 保证view不会创建多次,造成内存溢出
		// 2. 使用ViewHolder, 减少findviewbyid的次数
		// 3. 将ViewHolder写成static, 保证只在内存中加载一次
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.list_item_black_number, null);

				holder = new ViewHolder();

				TextView tvNumber = (TextView) view
						.findViewById(R.id.tv_number);
				TextView tvMode = (TextView) view.findViewById(R.id.tv_mode);
				ImageView ivDelete = (ImageView) view
						.findViewById(R.id.iv_delete);

				holder.tvNumber = tvNumber;
				holder.tvMode = tvMode;
				holder.ivDelete = ivDelete;

				view.setTag(holder);// 将holder对象,通过打标记的方式保存在view中, 和view绑定在一起了
				System.out.println("初始化view");
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();// 从view中取出holder对象
				System.out.println("重用view");
			}

			final BlackNumberInfo info = getItem(position);
			holder.tvNumber.setText(info.number);

			switch (info.mode) {
			case 1:
				holder.tvMode.setText("拦截电话");
				break;
			case 2:
				holder.tvMode.setText("拦截短信");
				break;
			case 3:
				holder.tvMode.setText("拦截全部");
				break;
			}

			// 给删除按钮添加点击事件
			holder.ivDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 1.从数据库删除
					// 2.从集合删除
					// 3.刷机listview
					mDao.delete(info.number);
					mList.remove(info);
					mAdapter.notifyDataSetChanged();
				}
			});

			return view;
		}
	}

	static class ViewHolder {
		public TextView tvNumber;
		public TextView tvMode;
		public ImageView ivDelete;
	}

	/**
	 * 添加黑名单
	 * 
	 * @param view
	 */
	public void addBlackNumber(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(this, R.layout.dialog_add_black_number, null);// 给dialog设定特定布局
		// dialog.setView(view);
		dialog.setView(view, 0, 0, 0, 0);// 去掉上下左右边距, 兼容2.x版本

		Button btnOK = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

		final EditText etBlackNumber = (EditText) view
				.findViewById(R.id.et_black_number);

		final RadioGroup rgGroup = (RadioGroup) view
				.findViewById(R.id.rg_group);

		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String phone = etBlackNumber.getText().toString().trim();
				if (!TextUtils.isEmpty(phone)) {
					int id = rgGroup.getCheckedRadioButtonId();// 获取当前被勾选的radiobutton的id
					int mode = 1;
					switch (id) {
					case R.id.rb_phone:
						mode = 1;
						break;
					case R.id.rb_sms:
						mode = 2;
						break;
					case R.id.rb_all:
						mode = 3;
						break;
					}

					mDao.add(phone, mode);// 保存到黑名单数据库中

					// 给集合添加元素
					BlackNumberInfo addInfo = new BlackNumberInfo();
					addInfo.number = phone;
					addInfo.mode = mode;

					// mList.add(addInfo);
					mList.add(0, addInfo);// 添加到集合的第一个位置

					// 刷新界面
					mAdapter.notifyDataSetChanged();

					dialog.dismiss();
				} else {
					ToastUtils.showToast(getApplicationContext(), "输入内容不能为空!");
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();

	}
}
