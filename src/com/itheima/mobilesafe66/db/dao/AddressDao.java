package com.itheima.mobilesafe66.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 归属地查询的数据库封装
 * 
 * @author Kevin
 * 
 */
public class AddressDao {

	private static final String PATH = "/data/data/com.itheima.mobilesafe66/files/address.db";

	/**
	 * 根据电话号码返回归属地 使用之前,必须在闪屏页先拷贝数据库
	 * 
	 * @param number
	 */
	public static String getAddress(String number) {
		String address = "未知号码";

		SQLiteDatabase database = SQLiteDatabase.openDatabase(PATH, null,
				SQLiteDatabase.OPEN_READONLY);// 打开数据库, 只支持从data/data目录打开,
												// 不能从assets打开

		// 要先判断是否是手机号码
		// 1 + [3-8] + 9位数字
		// 正则表达式 ^1[3-8]\d{9}$
		if (number.matches("^1[3-8]\\d{9}$")) {
			Cursor cursor = database
					.rawQuery(
							"select location from data2 where id=(select outkey from data1 where id=?)",
							new String[] { number.substring(0, 7) });

			if (cursor.moveToFirst()) {
				address = cursor.getString(0);
			}

			cursor.close();
		} else {
			switch (number.length()) {
			case 3:
				address = "报警电话";
				break;
			case 4:
				address = "模拟器";
				break;
			case 5:
				address = "客服电话";
				break;
			case 7:
			case 8:
				// 8888 8888
				address = "本地电话";
				break;
			default:
				// 010 8888 8888
				// 0910 8888 8888
				if (number.startsWith("0") && number.length() >= 11
						&& number.length() <= 12) {
					// 有可能是长途电话
					// 先查询4位区号
					Cursor cursor = database.rawQuery(
							"select location from data2 where area=?",
							new String[] { number.substring(1, 4) });

					if (cursor.moveToFirst()) {// 查到四位区号
						address = cursor.getString(0);
					}

					cursor.close();

					if ("未知号码".equals(address)) {
						// 没有查到4位区号,继续查3位区号
						cursor = database.rawQuery(
								"select location from data2 where area=?",
								new String[] { number.substring(1, 3) });

						if (cursor.moveToFirst()) {// 查到3位区号
							address = cursor.getString(0);
						}

						cursor.close();
					}
				}
				break;
			}
		}

		database.close();

		return address;
	}
}
