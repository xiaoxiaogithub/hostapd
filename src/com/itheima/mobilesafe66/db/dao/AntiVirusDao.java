package com.itheima.mobilesafe66.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 病毒的数据库封装
 * 
 * @author Kevin
 * 
 */
public class AntiVirusDao {

	private static final String PATH = "/data/data/com.itheima.mobilesafe66/files/antivirus.db";

	/**
	 * 根据apk的md5,判断是否是病毒
	 * 
	 * @param md5
	 * @return
	 */
	public static boolean isVirus(String md5) {
		SQLiteDatabase database = SQLiteDatabase.openDatabase(PATH, null,
				SQLiteDatabase.OPEN_READONLY);// 打开数据库, 只支持从data/data目录打开,
												// 不能从assets打开

		Cursor cursor = database.rawQuery("select * from datable where md5=?",
				new String[] { md5 });

		boolean isVirus = false;
		if (cursor.moveToFirst()) {
			isVirus = true;
		}

		cursor.close();
		database.close();

		return isVirus;
	}
}
