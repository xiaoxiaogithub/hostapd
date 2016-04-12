package com.itheima.mobilesafe66.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.db.dao.CommonNumberDao;
import com.itheima.mobilesafe66.db.dao.CommonNumberDao.ChildInfo;
import com.itheima.mobilesafe66.db.dao.CommonNumberDao.GroupInfo;

/**
 * 常用号码查询
 * 
 * @author Kevin
 * 
 */
public class CommonNumberActivity extends Activity {

	private ExpandableListView elvList;
	private CommonNumberAdapter mAdapter;
	private ArrayList<GroupInfo> mCommonNumerGroups;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_number);
		elvList = (ExpandableListView) findViewById(R.id.elv_list);

		// 从数据库中读取号码信息
		mCommonNumerGroups = CommonNumberDao.getCommonNumberGroups();

		mAdapter = new CommonNumberAdapter();
		elvList.setAdapter(mAdapter);

		elvList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				System.out.println("第" + groupPosition + "组-第" + childPosition
						+ "项被点击");
				ChildInfo child = mAdapter.getChild(groupPosition,
						childPosition);

				// 跳转到打电话页面
				// 需要权限:CALL_PHONE
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + child.number));
				startActivity(intent);
				return false;
			}
		});
	}

	class CommonNumberAdapter extends BaseExpandableListAdapter {

		// 返回组的数量
		@Override
		public int getGroupCount() {
			// return 8;
			return mCommonNumerGroups.size();
		}

		// 返回每个组孩子的数量
		@Override
		public int getChildrenCount(int groupPosition) {
			// return groupPosition + 1;
			return mCommonNumerGroups.get(groupPosition).children.size();
		}

		// 返回组的对象getItem()
		@Override
		public GroupInfo getGroup(int groupPosition) {
			return mCommonNumerGroups.get(groupPosition);
		}

		// 返回孩子的对象getItem()
		@Override
		public ChildInfo getChild(int groupPosition, int childPosition) {
			return mCommonNumerGroups.get(groupPosition).children
					.get(childPosition);
		}

		// 返回组id, getItemId()
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		// 返回孩子id, getItemId()
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		// 组的布局 getView
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView view = new TextView(getApplicationContext());
			view.setTextColor(Color.RED);
			view.setTextSize(20);
			// view.setText("       第" + groupPosition + "组");
			GroupInfo group = getGroup(groupPosition);
			view.setText("      " + group.name);
			return view;
		}

		// 孩子的布局 getView
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView view = new TextView(getApplicationContext());
			view.setTextColor(Color.BLACK);
			view.setTextSize(18);
			// view.setText("第" + groupPosition + "组-第" + childPosition + "项");
			ChildInfo child = getChild(groupPosition, childPosition);
			view.setText(child.name + "\n" + child.number);
			return view;
		}

		// 表示孩子是否可以点击
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}
