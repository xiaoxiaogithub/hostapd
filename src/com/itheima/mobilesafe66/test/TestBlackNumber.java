package com.itheima.mobilesafe66.test;

import java.util.Random;

import com.itheima.mobilesafe66.db.dao.BlackNumberDao;

import android.test.AndroidTestCase;

/**
 * 黑名单单元测试
 * 
 * @author Kevin
 * 
 */
public class TestBlackNumber extends AndroidTestCase {

	public void testAdd() {
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		// dao.add("110", 1);

		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			int mode = random.nextInt(3) + 1;
			if (i < 10) {
				dao.add("1341234567" + i, mode);
			} else {
				dao.add("135123456" + i, mode);
			}
		}
	}

	public void testDelete() {
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		dao.delete("110");
	}

	public void testUpdate() {
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		dao.update("110", 2);
	}

	public void testFind() {
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		boolean exist = dao.find("110");
		assertEquals(true, exist);// 参1:期望值, 参2:实际值
	}

}
