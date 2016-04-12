package com.itheima.mobilesafe66.receiver;

import com.itheima.mobilesafe66.service.UpdateWidgetService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * 窗口小部件组件 流程: 1. 写类继承AppWidgetProvider 2. 在清单文件中注册 3. 配置文件 4. widget布局文件
 * 
 */
public class MyWidget extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		System.out.println("onReceive");
	}

	// 第一次添加widget时
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		System.out.println("onEnabled");
		// 启动定时更新的服务
		context.startService(new Intent(context, UpdateWidgetService.class));
	}

	// 1. 新增widget, 2.间隔一定时间更新updatePeriodMillis
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		System.out.println("onUpdate");
		// 启动定时更新的服务
		context.startService(new Intent(context, UpdateWidgetService.class));
	}

	// widget被删除
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		System.out.println("onDeleted");
	}

	// 最后一个widget被移除
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		System.out.println("onDisabled");
		// 停止定时更新的服务
		context.stopService(new Intent(context, UpdateWidgetService.class));
	}
}
