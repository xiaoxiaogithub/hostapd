package com.itheima.mobilesafe66.receiver;

import com.itheima.mobilesafe66.engine.ProcessInfoProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 一键清理广播
 * 
 * @author Kevin
 * 
 */
public class KillReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ProcessInfoProvider.killAll(context);
	}

}
