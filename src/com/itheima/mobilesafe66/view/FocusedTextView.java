package com.itheima.mobilesafe66.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 强制获取焦点的TextView
 * 
 * @author Kevin
 * 
 */
public class FocusedTextView extends TextView {

	// 当布局文件中具有属性和样式style时, 系统底层解析时,就会走此构造方法
	public FocusedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		System.out.println("FocusedTextView style");
	}

	// 当布局文件中具有属性时, 系统底层解析时,就会走此构造方法
	public FocusedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.out.println("FocusedTextView AttributeSet");
	}

	// 从代码中new对象
	public FocusedTextView(Context context) {
		super(context);
		System.out.println("FocusedTextView context");
	}

	@Override
	public boolean isFocused() {
		return true;// 强制让TextView具有焦点
	}

}
