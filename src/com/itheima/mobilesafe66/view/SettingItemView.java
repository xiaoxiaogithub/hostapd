package com.itheima.mobilesafe66.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe66.R;

/**
 * 自定义组合控件
 * 
 * 1. 写一个类继承RelativeLayout(ViewGroup)
 * 2. 写布局文件
 * 3. 将布局添加到RelativeLayout中(initView方法)
 * 4. 增加api
 * 5. 自定义属性(1. values/attrs.xml, 2. 声明命名空间 , 3.在自定义view中配置属性, 4. 在自定义view中加载属性值 )
 * @author Kevin
 * 
 */
public class SettingItemView extends RelativeLayout {

	private static final String NAMESPACE = "http://schemas.android.com/apk/res/com.itheima.mobilesafe66";
	private TextView tvTitle;
	private TextView tvDesc;
	private CheckBox cbCheck;
	private String mDescOn;
	private String mDescOff;

	public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public SettingItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();

		// int count = attrs.getAttributeCount();
		// for (int i = 0; i < count; i++) {
		// String attributeName = attrs.getAttributeName(i);
		// String attributeValue = attrs.getAttributeValue(i);
		// System.out.println(attributeName + "=" + attributeValue);
		// }

		String title = attrs.getAttributeValue(NAMESPACE, "title");
		mDescOn = attrs.getAttributeValue(NAMESPACE, "desc_on");
		mDescOff = attrs.getAttributeValue(NAMESPACE, "desc_off");

		setTitle(title);
	}

	public SettingItemView(Context context) {
		super(context);
		initView();
	}

	/**
	 * 初始化布局
	 */
	private void initView() {
		View child = View.inflate(getContext(), R.layout.setting_item_view,
				null);// 初始化组合控件布局

		tvTitle = (TextView) child.findViewById(R.id.tv_title);
		tvDesc = (TextView) child.findViewById(R.id.tv_desc);
		cbCheck = (CheckBox) child.findViewById(R.id.cb_check);

		this.addView(child);// 将布局添加给当前的RelativeLayout对象
	}

	/**
	 * 设置标题
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	/**
	 * 设置表述
	 * 
	 * @param desc
	 */
	public void setDesc(String desc) {
		tvDesc.setText(desc);
	}

	/**
	 * 判断是否勾选
	 * 
	 * @return
	 */
	public boolean isChecked() {
		return cbCheck.isChecked();
	}

	/**
	 * 设置选中状态
	 * 
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		cbCheck.setChecked(checked);

		// 更新描述信息
		if (checked) {
			setDesc(mDescOn);
		} else {
			setDesc(mDescOff);
		}
	}

}
