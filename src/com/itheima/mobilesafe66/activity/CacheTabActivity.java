package com.itheima.mobilesafe66.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.itheima.mobilesafe66.R;

/**
 * 具有标签功能的activity
 * 
 * @author Kevin
 * @date 2015-8-5
 */
public class CacheTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cache_tab);

		TabHost tabHost = getTabHost();

		// 初始化一个标签
		TabSpec tab1 = tabHost.newTabSpec("Clean_Cache");
		tab1.setIndicator("缓存清理");// 系统默认样式
		tab1.setContent(new Intent(this, CleanCacheActivity.class));// 页签点击后跳转缓存清理页面

		// 初始化一个标签
		TabSpec tab2 = tabHost.newTabSpec("Sdcard_Cache");
		tab2.setIndicator("sdcard清理");// 系统默认样式
		tab2.setContent(new Intent(this, SdcardCacheActivity.class));// 页签点击后跳转sdcard清理页面

		tabHost.addTab(tab1);
		tabHost.addTab(tab2);
	}
}
