package org.seckill.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 配置spring和junit整合，这样junit在启动时就会加载spring容器
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
	
	@Resource
	private SuccessKilledDao successKilledDao;
	
	//int insertSuccessKilled(long seckillId,long userPhone);
	@Test
	public void testInsertSuccessKilled() {
		long id = 1000L;
		long userPhone = 12234325566L;
		int insertCount = successKilledDao.insertSuccessKilled(id, userPhone);
		System.out.println("insertCount="+insertCount);
		
		
	}
	
	//SuccessKilled queryByIdWithSeckill(long seckillId,long userPhone);
	@Test
	public void testQueryByIdWithSeckill() {
		long id = 1000L;
		long userPhone = 12234325566L;
		System.out.println(successKilledDao.queryByIdWithSeckill(id, userPhone).getSeckill());
		System.out.println(successKilledDao.queryByIdWithSeckill(id, userPhone));
	}

}
