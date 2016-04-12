package com.itheima.mobilesafe66.activity;

import com.itheima.mobilesafe66.R;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;

/**
 * 流量统计
 * 
 * @author Kevin
 * @date 2015-8-4
 */
public class TrafficStatsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_stats);

		String totalRxBytes = "总下载流量:" + TrafficStats.getTotalRxBytes();// wifi+3g
		String totalTxBytes = "总上传流量:" + TrafficStats.getTotalTxBytes();// wifi+3g

		String mobileRxBytes = "移动下载流量" + TrafficStats.getMobileRxBytes();// 3g下载流量
		String mobileTxBytes = "移动上传流量" + TrafficStats.getMobileTxBytes();// 3g下载流量

		// 具体微信uid以真机为准
		String uidRxBytes = "微信下载流量:" + TrafficStats.getUidRxBytes(10088);// wifi+3g
		String uidTxBytes = "微信上传流量:" + TrafficStats.getUidTxBytes(10088);// wifi+3g

		System.out.println(totalRxBytes);
		System.out.println(totalTxBytes);
		System.out.println(mobileRxBytes);
		System.out.println(mobileTxBytes);
		System.out.println(uidRxBytes);
		System.out.println(uidTxBytes);
	}
}
