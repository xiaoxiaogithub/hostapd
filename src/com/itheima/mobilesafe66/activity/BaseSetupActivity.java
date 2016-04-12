package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.itheima.mobilesafe66.utils.ToastUtils;

/**
 * 设置向导的基类
 * 
 * @author Kevin
 * 
 */
public abstract class BaseSetupActivity extends Activity {

	// 手势识别器
	private GestureDetector mDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					/**
					 * 快速滑动,抛 e1: 起点坐标 e2: 终点坐标 velocityX: 水平滑动速度 velocityY:
					 * 竖直滑动速度
					 */
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {

						if (Math.abs(e2.getRawY() - e1.getRawY()) > 100) {// 竖直方向滑动范围太大
							ToastUtils.showToast(getApplicationContext(),
									"不能这样划哦!");
							return true;
						}

						if (Math.abs(velocityX) < 100) {
							ToastUtils.showToast(getApplicationContext(),
									"滑动太慢了哦!");
							return true;
						}

						// 判断向左划还是想右划
						// e1.getX();//相对父控件的x坐标
						// e1.getRawX();//屏幕的绝对坐标
						if (e2.getRawX() - e1.getRawX() > 200) {// 向右划,上一页
							System.out.println("上一页");
							showPrevious();
							return true;
						}

						if (e1.getRawX() - e2.getRawX() > 200) {// 向左划, 下一页
							System.out.println("下一页");
							showNext();
							return true;
						}

						return super.onFling(e1, e2, velocityX, velocityY);
					}
				});
	}

	/**
	 * 按钮点击上一页
	 * 
	 * @param view
	 */
	public void previous(View view) {
		showPrevious();
	}

	/**
	 * 按钮点击下一页
	 * 
	 * @param view
	 */
	public void next(View view) {
		showNext();
	}

	/**
	 * 跳转上一页
	 */
	public abstract void showPrevious();

	/**
	 * 跳转下一页
	 */
	public abstract void showNext();

	/**
	 * 当前界面被触摸时,走此方法
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);// 将事件委托给手势识别器处理
		return super.onTouchEvent(event);
	}
}
