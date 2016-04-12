package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;
import com.itheima.mobilesafe66.utils.PrefUtils;

/**
 * 归属地位置修改页面
 * 
 * 实现半透明效果 1. 设置主题, 成为全透明 <activity
 * android:name="com.itheima.mobilesafe66.activity.DragViewActivity"
 * android:theme="@android:style/Theme.Translucent.NoTitleBar" > </activity> 2.
 * 设置根布局颜色为半透明
 * 
 * @author Kevin
 * 
 */
public class DragViewActivity extends Activity {

	private ImageView ivDrag;

	private int startX;
	private int startY;

	private int mScreenWidth;
	private int mScreenHeight;

	private TextView tvTop;
	private TextView tvBottom;

	long[] mHits = new long[2];// 数组长度就是多击次数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drag_view);

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		mScreenWidth = wm.getDefaultDisplay().getWidth();// 屏幕宽度
		mScreenHeight = wm.getDefaultDisplay().getHeight();// 屏幕高度

		ivDrag = (ImageView) findViewById(R.id.iv_drag);
		tvTop = (TextView) findViewById(R.id.tv_top);
		tvBottom = (TextView) findViewById(R.id.tv_bottom);

		int lastX = PrefUtils.getInt("lastX", 0, this);
		int lastY = PrefUtils.getInt("lastY", 0, this);

		// 根据当前位置,显示文本框提示
		if (lastY > mScreenHeight / 2) {
			// 下方
			tvTop.setVisibility(View.VISIBLE);
			tvBottom.setVisibility(View.INVISIBLE);
		} else {
			// 上方
			tvTop.setVisibility(View.INVISIBLE);
			tvBottom.setVisibility(View.VISIBLE);
		}

		// measure(测量宽高)->layout(设定位置)->draw(绘制), 这三个步骤必须在oncreate方法结束之后才调用
		// ivDrag.layout(lastX, lastY, lastX + ivDrag.getWidth(),
		// lastY + ivDrag.getHeight());//此方法不能在oncreate中执行,因为布局还没有开始绘制

		// 通过修改布局参数,设定位置
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivDrag
				.getLayoutParams();// 布局的父控件是谁,就获取谁的布局参数
		// int topMargin = params.topMargin;
		// System.out.println("top margin->" + topMargin);
		// 临时修改了布局参数, 通过布局参数修改布局位置
		params.topMargin = lastY;
		params.leftMargin = lastX;

		// 触摸事件监听
		ivDrag.setOnTouchListener(new OnTouchListener() {

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
					int l = ivDrag.getLeft() + dx;// 最新的左边距
					int r = ivDrag.getRight() + dx;
					int t = ivDrag.getTop() + dy;
					int b = ivDrag.getBottom() + dy;

					// 防止布局超出屏幕边界
					if (l < 0 || r > mScreenWidth) {
						return true;
					}
					// 防止布局超出屏幕边界
					if (t < 0 || b > mScreenHeight - 25) {// 需要减掉状态栏的高度
						return true;
					}

					// 根据当前位置,显示文本框提示
					if (t > mScreenHeight / 2) {
						// 下方
						tvTop.setVisibility(View.VISIBLE);
						tvBottom.setVisibility(View.INVISIBLE);
					} else {
						// 上方
						tvTop.setVisibility(View.INVISIBLE);
						tvBottom.setVisibility(View.VISIBLE);
					}

					ivDrag.layout(l, t, r, b);// 根据最新边距修改布局位置

					// 重新初始化起始点坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					// System.out.println("抬起事件");
					// 保存当前位置
					PrefUtils.putInt("lastX", ivDrag.getLeft(),
							getApplicationContext());
					PrefUtils.putInt("lastY", ivDrag.getTop(),
							getApplicationContext());
					break;

				default:
					break;
				}

				// return true;// 返回true表示要消耗掉事件
				return false;// 当同时设置onTouch和onClick时, 返回false可以保证这两个都响应
			}
		});

		ivDrag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 数组拷贝:参1:原数组;参2:原数组拷贝起始位置;参3:目标数组;参4:目标数组起始拷贝位置;参5:拷贝数组长度
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();// 手机开机时间
				if (SystemClock.uptimeMillis() - mHits[0] <= 500) {
					// 布局居中显示
					ivDrag.layout(mScreenWidth / 2 - ivDrag.getWidth() / 2,
							ivDrag.getTop(),
							mScreenWidth / 2 + ivDrag.getWidth() / 2,
							ivDrag.getBottom());
				}
			}
		});
	}
}
