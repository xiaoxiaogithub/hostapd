package com.itheima.mobilesafe66.db.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.itheima.mobilesafe66.db.AppLockOpenHelper;

/**
 * 程序锁增删改查封装 crud
 * 
 * @author Kevin
 * 
 */
public class AppLockDao {
	// 3. 声明一个对象
	private static AppLockDao sInstance = null;

	private AppLockOpenHelper mHelper;

	private Context mContext;

	// 1. 私有的构造方法
	private AppLockDao(Context ctx) {
		mHelper = new AppLockOpenHelper(ctx);
		mContext = ctx;
	}

	// 2. 公开方法获取对象
	public static AppLockDao getInstance(Context ctx) {
		if (sInstance == null) {
			// A, B
			synchronized (AppLockDao.class) {
				if (sInstance == null) {
					sInstance = new AppLockDao(ctx);
				}
			}
		}

		return sInstance;
	}

	/**
	 * 增加已加锁
	 * 
	 * @param number
	 * @param mode
	 */
	public void add(String packageName) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("package", packageName);
		database.insert("applock", null, values);
		database.close();

		// 通知数据库发生变化
		mContext.getContentResolver().notifyChange(
				Uri.parse("content://com.itheima.mobilesafe66/change"), null);
	}

	/**
	 * 删除已加锁
	 * 
	 * @param number
	 */
	public void delete(String packageName) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		database.delete("applock", "package=?", new String[] { packageName });
		database.close();

		// 通知数据库发生变化
		mContext.getContentResolver().notifyChange(
				Uri.parse("content://com.itheima.mobilesafe66/change"), null);
	}

	/**
	 * 查询是否已加锁
	 * 
	 * @param number
	 */
	public boolean find(String packageName) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database.query("applock", new String[] { "package" },
				"package=?", new String[] { packageName }, null, null, null);

		boolean exist = false;
		if (cursor.moveToFirst()) {
			exist = true;
		}

		cursor.close();
		database.close();
		return exist;
	}

	/**
	 * 查询所有已加锁app
	 * 
	 * @return
	 */
	public ArrayList<String> findAll() {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database.query("applock", new String[] { "package", },
				null, null, null, null, null);

		ArrayList<String> list = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String packageName = cursor.getString(0);
			list.add(packageName);
		}

		cursor.close();
		database.close();

		return list;
	}
}
