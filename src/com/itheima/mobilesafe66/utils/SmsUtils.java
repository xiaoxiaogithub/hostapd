package com.itheima.mobilesafe66.utils;

import java.io.File;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;

/**
 * 短信备份的工具类
 * 
 * 权限: <uses-permission android:name="android.permission.READ_SMS" />
 * <uses-permission android:name="android.permission.WRITE_SMS" />
 * 
 * @author Kevin
 * 
 */
public class SmsUtils {

	public static void smsBackup(Context ctx, File output, SmsCallback callback) {
		// 从系统短信数据库读取短信内容
		// 将短信序列化到xml文件中
		// ProgressDialog和进度相关的方法可以在子线程运行
		try {
			// address:来电号码
			// date:短信时间
			// read:是否已读, 1表示已读, 0表示未读
			// type:标记短信类型, 1表示收到, 2表示发出
			// body:短信内容
			Cursor cursor = ctx.getContentResolver().query(
					Uri.parse("content://sms"),
					new String[] { "address", "date", "type", "body" }, null,
					null, null);

			//dialog.setMax(cursor.getCount());// 将短信数量设置为进度最大值
			callback.preSmsBackup(cursor.getCount());//回调短信总数

			XmlSerializer xml = Xml.newSerializer();
			xml.setOutput(new FileOutputStream(output), "utf-8");
			xml.startDocument("utf-8", false);
			xml.startTag(null, "smss");

			int progress = 0;
			while (cursor.moveToNext()) {
				xml.startTag(null, "sms");

				xml.startTag(null, "address");
				String address = cursor.getString(cursor
						.getColumnIndex("address"));
				xml.text(address);
				xml.endTag(null, "address");

				xml.startTag(null, "date");
				String date = cursor.getString(cursor.getColumnIndex("date"));
				xml.text(date);
				xml.endTag(null, "date");

				xml.startTag(null, "type");
				String type = cursor.getString(cursor.getColumnIndex("type"));
				xml.text(type);
				xml.endTag(null, "type");

				xml.startTag(null, "body");
				String body = cursor.getString(cursor.getColumnIndex("body"));
				xml.text(body);
				xml.endTag(null, "body");

				xml.endTag(null, "sms");

				progress++;
				//dialog.setProgress(progress);// 更新进度
				callback.onSmsBackup(progress);//回调更新进度
				
				Thread.sleep(500);//模拟耗时操作
			}

			xml.endTag(null, "smss");
			xml.endDocument();

			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//短信备份的回调接口
	public interface SmsCallback {
		//备份前, count表示短信总数
		public void preSmsBackup(int count);
		//正在备份, progress备份进度
		public void onSmsBackup(int progress);
	}
}
