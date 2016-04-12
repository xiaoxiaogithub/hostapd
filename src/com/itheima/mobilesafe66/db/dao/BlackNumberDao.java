package com.itheima.mobilesafe66.db.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itheima.mobilesafe66.db.BlackNumberOpenHelper;
import com.itheima.mobilesafe66.domain.BlackNumberInfo;

/**
 * 黑名单增删改查封装 crud
 * 
 * @author Kevin
 * 
 */
public class BlackNumberDao {
	// 3. 声明一个对象
	private static BlackNumberDao sInstance = null;

	private BlackNumberOpenHelper mHelper;

	// 1. 私有的构造方法
	private BlackNumberDao(Context ctx) {
		mHelper = new BlackNumberOpenHelper(ctx);
	}

	// 2. 公开方法获取对象
	public static BlackNumberDao getInstance(Context ctx) {
		if (sInstance == null) {
			// A, B
			synchronized (BlackNumberDao.class) {
				if (sInstance == null) {
					sInstance = new BlackNumberDao(ctx);
				}
			}
		}

		return sInstance;
	}

	/**
	 * 增加黑名单
	 * 
	 * @param number
	 * @param mode
	 */
	public void add(String number, int mode) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		database.insert("blacknumber", null, values);
		database.close();
	}

	/**
	 * 删除黑名单
	 * 
	 * @param number
	 */
	public void delete(String number) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		database.delete("blacknumber", "number=?", new String[] { number });
		database.close();
	}

	/**
	 * 更新拦截模式
	 * 
	 * @param number
	 * @param mode
	 */
	public void update(String number, int mode) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", mode);
		database.update("blacknumber", values, "number=?",
				new String[] { number });
		database.close();
	}

	/**
	 * 查询黑名单是否存在
	 * 
	 * @param number
	 */
	public boolean find(String number) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database
				.query("blacknumber", new String[] { "number", "mode" },
						"number=?", new String[] { number }, null, null, null);

		boolean exist = false;
		if (cursor.moveToFirst()) {
			exist = true;
		}

		cursor.close();
		database.close();
		return exist;
	}

	/**
	 * 根据电话号码查询拦截模式
	 * 
	 * @param number
	 */
	public int findMode(String number) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database.query("blacknumber", new String[] { "mode" },
				"number=?", new String[] { number }, null, null, null);

		int mode = -1;
		if (cursor.moveToFirst()) {
			mode = cursor.getInt(0);
		}

		cursor.close();
		database.close();
		return mode;
	}

	/**
	 * 查询所有黑名单
	 * 
	 * @return
	 */
	public ArrayList<BlackNumberInfo> findAll() {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database.query("blacknumber", new String[] { "number",
				"mode" }, null, null, null, null, null);

		ArrayList<BlackNumberInfo> list = new ArrayList<BlackNumberInfo>();
		while (cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(0);
			int mode = cursor.getInt(1);
			info.number = number;
			info.mode = mode;
			list.add(info);
		}

		cursor.close();
		database.close();

		return list;
	}

	/**
	 * 分页查询黑名单
	 * 
	 * @return
	 */
	public ArrayList<BlackNumberInfo> findPart(int index) {
		SQLiteDatabase database = mHelper.getWritableDatabase();

		Cursor cursor = database.rawQuery(
				"select number,mode from blacknumber order by _id desc limit ?,20",
				new String[] { index + "" });// 分页查询20条数据,逆序排列,最后添加的展示在最上面

		ArrayList<BlackNumberInfo> list = new ArrayList<BlackNumberInfo>();
		while (cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(0);
			int mode = cursor.getInt(1);
			info.number = number;
			info.mode = mode;
			list.add(info);
		}

		cursor.close();
		database.close();

		return list;
	}

	/**
	 * 获取黑名单总个数
	 * 
	 * @return
	 */
	public int getTotalCount() {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select count(*) from blacknumber",
				null);

		int count = -1;
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}

		cursor.close();
		database.close();
		return count;
	}
}
