package com.itheima.mobilesafe66.db.dao;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 归属地查询的数据库封装
 * 
 * @author Kevin
 * 
 */
public class CommonNumberDao {

	private static final String PATH = "/data/data/com.itheima.mobilesafe66/files/commonnum.db";

	/**
	 * 获取常用号码组的信息
	 */
	public static ArrayList<GroupInfo> getCommonNumberGroups() {
		SQLiteDatabase database = SQLiteDatabase.openDatabase(PATH, null,
				SQLiteDatabase.OPEN_READONLY);// 打开数据库, 只支持从data/data目录打开,
												// 不能从assets打开
		Cursor cursor = database.query("classlist", new String[] { "name",
				"idx" }, null, null, null, null, null);

		ArrayList<GroupInfo> list = new ArrayList<CommonNumberDao.GroupInfo>();
		while (cursor.moveToNext()) {
			GroupInfo info = new GroupInfo();
			String name = cursor.getString(0);
			String idx = cursor.getString(1);

			info.name = name;
			info.idx = idx;
			info.children = getCommonNumberChildren(idx, database);

			list.add(info);
		}

		cursor.close();
		database.close();

		return list;
	}

	/**
	 * 获取某个组孩子的信息
	 * 
	 * @param idx
	 * @param database
	 * @return
	 */
	public static ArrayList<ChildInfo> getCommonNumberChildren(String idx,
			SQLiteDatabase database) {
		Cursor cursor = database.query("table" + idx, new String[] { "number",
				"name" }, null, null, null, null, null);

		ArrayList<ChildInfo> list = new ArrayList<CommonNumberDao.ChildInfo>();
		while (cursor.moveToNext()) {
			ChildInfo info = new ChildInfo();
			String number = cursor.getString(0);
			String name = cursor.getString(1);
			info.number = number;
			info.name = name;

			list.add(info);
		}

		cursor.close();
		return list;
	}

	public static class GroupInfo {
		public String name;
		public String idx;
		public ArrayList<ChildInfo> children;
	}

	public static class ChildInfo {
		public String name;
		public String number;
	}
}
