package org.seckill.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml" })
public class SeckillServiceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SeckillService seckillService;

	@Test
	public void testGetSeckillList() {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}", list);
	}

	@Test
	public void testGetById() {
		long id = 1000;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill={}", seckill);
	}

	// 测试代码完整逻辑，注意可重复执行
	@Test
	public void testSeckillLogin() {
		long id = 1000;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		if (exposer.isExposed()) {
			logger.info("exposer={}", exposer);
			long userPhone = 12233455667L;
			String md5 = exposer.getMd5();
			try {
				SeckillExecution seckillExection = seckillService.executeSeckill(id, userPhone, md5);
				logger.info("result={}", seckillExection);
			} catch (RepeatKillException e) {
				logger.error(e.getMessage());
			} catch (SeckillCloseException e) {
				logger.error(e.getMessage());
			}
		} else {
			// 秒杀未开启
			logger.warn("exposer={}", exposer);
		}
	}

	/**
	 * 测试方法，缺点：抛出异常没有捕获导致测试未通过
	 */
	// @Test
	// public void testExecuteSeckill() {
	// long id = 1000;
	// long userPhone = 12233455667L;
	// String md5 = "7f390e13f599e975b735e6451df86da2";
	// SeckillExection seckillExection = seckillService.executeSeckill(id,
	// userPhone, md5);
	// logger.info("result={}",seckillExection);
	// }

	/**
	 * public SeckillExecution executeSeckillProcedure( long seckillId, long
	 * userPhone, String md5)
	 */

	@Test
	public void executeSeckillProcedure() {
		long seckillId = 1003;
		long phone = 13223334458L;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if (exposer.isExposed()) {
			String md5 = exposer.getMd5();
			SeckillExecution execution 
					= seckillService.executeSeckillProcedure(seckillId, phone, md5);
			logger.info(execution.getStateInfo());
		}
	}
}
