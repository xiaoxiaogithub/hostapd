package com.itheima.mobilesafe66.activity;

import android.app.Activity;
import android.os.Bundle;

import com.itheima.mobilesafe66.R;

/**
 * sdcard缓存清理
 * 
 * @author Kevin
 * @date 2015-8-4
 */
public class SdcardCacheActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sdcard_cache);
		
		//1. 读取sdcard缓存数据库,获取缓存目录
		//2. 判断该目录是否在本地存在
		//3. 如果存在,就删除该文件夹
		
		//File file = new File("");
		//file.exists();
		
		//递归删除文件夹中所有文件
		//File[] listFiles = file.listFiles();
		//file.delete()
	}
}
